package com.photocoach.app.research

enum class MissingChoiceRule { EXCLUDE, COUNT_AS_NOT_PREFERRED }
enum class TieChoiceRule { EXCLUDE, HALF, COUNT_AS_NOT_PREFERRED }
enum class FailedTimeRule { EXCLUDE_PAIR, USE_DEADLINE }
enum class IncrementBaseline { DIRECT_DYNAMIC_STATIC_ABOVE_HALF, DIFFERENCE_VERSUS_SYSTEM }
data class CalculationPolicy(val excludeExits:Boolean,val missing:MissingChoiceRule,val ties:TieChoiceRule,
    val failedTime:FailedTimeRule,val increment:IncrementBaseline,val deadlineMs:Long,
    val d7Start:Int,val d7End:Int,val d7IncludeLost:Boolean,val cueWindowMs:Long)
data class Fraction(val numerator:Double,val denominator:Int) { val value:Double? get()=if(denominator>0)numerator/denominator else null }
data class TrialMeasurement(val registration:RegisteredTrial,val replay:RecomputedRound,val requiredSlots:Set<Int>)
data class CueReview(val id:String,val improvedInMs:Long?,val observable:Boolean,val errors:Set<String>,val reviewed:Boolean)
data class VoiceAnswer(val id:String,val disturbed:Boolean?)
data class PerformanceSample(val id:String,val kind:String,val durationMs:Long?)
data class OfflineAttempt(val id:String,val completed:Boolean)
data class CalculatedResearchMetrics(val withinDeadline:Fraction,val twoSteps:Fraction,
    val preferencesByAudience:Map<String,Fraction>,val incrementByAudience:Map<String,Double?>,
    val pairedTimeReduction:Double?,val naturalReuse:Fraction,val cueImprovement:Fraction,val cueErrors:Fraction,
    val disturbance:Fraction,val p95ByKind:Map<String,Long?>,val unfinishedPerformance:Int,
    val firstSave:Fraction,val recovery:Fraction,val offline:Fraction,
    val missingAnswers:Int,val unobservableCues:Int,val excludedTimePairs:Int)
data class MetricCalculation(val metrics:CalculatedResearchMetrics?,val blockers:List<String>)

/** Descriptive calculation with explicit policy. No Go evaluator or statistical-independence claim. */
object ResearchMetricCalculation {
    fun calculate(trials:List<TrialMeasurement>,ratings:List<BlindRating>,followUps:List<FollowUp>,
        cues:List<CueReview>,answers:List<VoiceAnswer>,performance:List<PerformanceSample>,offline:List<OfflineAttempt>,
        policy:CalculationPolicy?,pairs:List<RegisteredBlindPair>?=null):MetricCalculation {
        if(policy==null)return MetricCalculation(null,listOf("calculation policies are not supplied"))
        val problems=mutableListOf<String>()
        if(policy.deadlineMs<=0 || policy.cueWindowMs<=0 || policy.d7Start<0 || policy.d7End<policy.d7Start)problems+="invalid calculation policy"
        val registrations=trials.map {it.registration}
        problems+=ResearchSideContract.inspect(registrations,ratings,followUps,pairs=pairs).issues
        if(ratings.isNotEmpty() && pairs==null)problems+="pre-registered blind pair roster is required"
        if(trials.any {it.registration.key!=it.replay.key || it.replay.outcome==ResearchRoundOutcome.INCOMPLETE ||
                (it.replay.captureLatencyMs ?: 0)<0 || it.requiredSlots.any {slot -> slot<1}})problems+="incomplete or unlinked trial"
        fun duplicates(ids:List<String>)=ids.any {it.isBlank()} || ids.distinct().size!=ids.size
        if(duplicates(cues.map {it.id}) || duplicates(answers.map {it.id}) || duplicates(performance.map {it.id}) || duplicates(offline.map {it.id}))
            problems+="duplicate measurement identity"
        if(cues.any {(it.improvedInMs ?: 0)<0} || performance.any {(it.durationMs ?: 0)<0})problems+="invalid measurement time"
        if(problems.isNotEmpty())return MetricCalculation(null,problems.distinct())
        val eligible=trials.filterNot {policy.excludeExits && it.replay.outcome==ResearchRoundOutcome.EXITED}
        val dynamic=eligible.filter {it.registration.condition=="DYNAMIC"}
        val successes=dynamic.filter {it.replay.outcome==ResearchRoundOutcome.SUCCESS && it.replay.captureLatencyMs!=null && !it.replay.exitedBeforePublication}
        fun ratio(n:Int,d:Int)=Fraction(n.toDouble(),d)
        val byKey=trials.associateBy {it.registration.key}
        val completeRatings=ResearchSideContract.completeRatings(ratings,pairs.orEmpty())
        fun preference(audience:String,condition:String,against:String):Fraction {
            var votes=0.0;var denominator=0
            completeRatings.filter {it.audience==audience}.forEach {r ->
                val first=byKey.getValue(r.first).registration.condition;val second=byKey.getValue(r.second).registration.condition
                if(setOf(first,second)!=setOf(condition,against))return@forEach
                val choice=if(!r.imagesPresent)BlindChoice.MISSING else r.choice
                if(choice==BlindChoice.MISSING && policy.missing==MissingChoiceRule.EXCLUDE || choice==BlindChoice.TIE && policy.ties==TieChoiceRule.EXCLUDE)return@forEach
                denominator++
                votes+=when(choice){BlindChoice.FIRST->if(first==condition)1.0 else 0.0
                    BlindChoice.SECOND->if(second==condition)1.0 else 0.0
                    BlindChoice.TIE->if(policy.ties==TieChoiceRule.HALF).5 else 0.0
                    BlindChoice.MISSING->0.0}
            }
            return Fraction(votes,denominator)
        }
        val roles=listOf("shooter","subject")
        val preferences=roles.associateWith {preference(it,"DYNAMIC","SYSTEM")}
        val increments=roles.associateWith {role -> when(policy.increment) {
            IncrementBaseline.DIRECT_DYNAMIC_STATIC_ABOVE_HALF->preference(role,"DYNAMIC","STATIC").value?.minus(.5)
            IncrementBaseline.DIFFERENCE_VERSUS_SYSTEM->{val d=preferences.getValue(role).value;val s=preference(role,"STATIC","SYSTEM").value
                if(d==null||s==null)null else d-s}
        }}
        val durations=mutableListOf<Pair<Long,Long>>();var excluded=0
        eligible.groupBy {it.registration.participant to it.registration.scene}.values.forEach {group ->
            val d=group.singleOrNull {it.registration.condition=="DYNAMIC"};val s=group.singleOrNull {it.registration.condition=="STATIC"}
            if(d==null||s==null){excluded++;return@forEach}
            fun duration(t:TrialMeasurement):Long?=if(t.replay.outcome==ResearchRoundOutcome.SUCCESS && !t.replay.exitedBeforePublication)
                t.replay.captureLatencyMs else if(policy.failedTime==FailedTimeRule.USE_DEADLINE)policy.deadlineMs else null
            val dt=duration(d);val st=duration(s)
            if(dt==null||st==null)excluded++ else durations+=dt to st
        }
        fun median(values:List<Long>):Double? {if(values.isEmpty())return null;val v=values.sorted();val i=v.size/2;return if(v.size%2==1)v[i].toDouble() else v[i-1]/2.0+v[i]/2.0}
        val dMedian=median(durations.map {it.first});val sMedian=median(durations.map {it.second})
        val participants=registrations.map {it.participant}.toSet()
        val observed=followUps.filter {it.observedDay!=null}
        val d7denom=if(policy.d7IncludeLost)participants.size else observed.size
        val observable=cues.filter {it.observable};val reviewed=cues.filter {it.reviewed};val validAnswers=answers.filter {it.disturbed!=null}
        val accepted=trials.flatMap {it.replay.captures}
        val failedSaves=accepted.filter {it.saveFailed && !it.captureFailed}
        return MetricCalculation(CalculatedResearchMetrics(
            ratio(dynamic.count {it.replay.outcome==ResearchRoundOutcome.SUCCESS && !it.replay.exitedBeforePublication &&
                it.replay.captureLatencyMs?.let {ms -> ms<=policy.deadlineMs}==true},dynamic.size),
            ratio(successes.count {it.requiredSlots.size<=2},successes.size),preferences,increments,
            if(dMedian!=null && sMedian!=null && sMedian>0)1-dMedian/sMedian else null,
            ratio(observed.count {it.realCapture && !it.scheduledByResearch && it.observedDay!! in policy.d7Start..policy.d7End},d7denom),
            ratio(observable.count {it.improvedInMs?.let {ms->ms<=policy.cueWindowMs}==true},observable.size),
            ratio(reviewed.count {it.errors.isNotEmpty()},reviewed.size),ratio(validAnswers.count {it.disturbed==true},validAnswers.size),
            performance.groupBy {it.kind}.mapValues {(_,rows)-> val values=rows.mapNotNull {it.durationMs}.sorted()
                if(values.isEmpty())null else values[(kotlin.math.ceil(values.size*.95).toInt()-1).coerceAtLeast(0)]},
            performance.count {it.durationMs==null},ratio(accepted.count {it.firstAttemptSucceeded},accepted.size),
            ratio(failedSaves.count {it.recovered},failedSaves.size),ratio(offline.count {it.completed},offline.size),
            answers.count {it.disturbed==null},cues.count {!it.observable},excluded),emptyList())
    }
}
