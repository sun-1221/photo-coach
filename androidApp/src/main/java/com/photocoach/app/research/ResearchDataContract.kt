package com.photocoach.app.research

import kotlinx.serialization.json.Json

data class ResearchLogImport(val events: List<ResearchEvent>, val issues: List<String>) {
    val complete: Boolean get() = issues.isEmpty()
}

/** Reads the actual logger format. Corruption is evidence, never an silently excluded trial. */
object ResearchLogReader {
    fun read(jsonl: String, selectedSessions:Set<String>? = null): ResearchLogImport {
        val events = mutableListOf<ResearchEvent>()
        val issues = mutableListOf<String>()
        jsonl.lineSequence().filter { it.isNotBlank() }.forEachIndexed { index, line ->
            runCatching { Json.decodeFromString<ResearchEvent>(line) }
                .onSuccess { events += it }.onFailure { issues += "invalid JSONL row ${index + 1}" }
        }
        if (events.isEmpty()) issues += "no events"
        if (jsonl.isNotEmpty() && !jsonl.endsWith("\n")) issues += "unterminated last JSONL row"
        if(selectedSessions!=null) {
            if(selectedSessions.isEmpty() || !events.map {it.sessionId}.containsAll(selectedSessions))issues+="registered session absent from export"
            events.removeAll {it.sessionId !in selectedSessions}
        }
        if(events.any {it.type=="recovery_context_unavailable"})issues+="recovery lacks durable research association"
        events.groupBy { it.sessionId }.forEach { (session, rows) ->
            if (session.isBlank() || session == "unknown") issues += "missing anonymous session"
            var previous = -1L
            rows.distinct().forEach { event ->
                if (event.sequence == null || event.sequence != previous + 1) issues += "sequence gap or reorder: $session"
                previous = event.sequence ?: previous
                if (event.previousFailedWrites > 0 || event.previousDroppedEvents > 0) issues += "lost events: $session"
                if (event.elapsedMs < 0 || event.occurredAtMs < 0 || event.roundId < 1) issues += "invalid time/round: $session"
                if (event.condition !in setOf("STATIC", "DYNAMIC") || event.scene !in setOf("WINDOW", "SCENERY", "BACKLIGHT") ||
                    event.configurationId.isBlank() || event.buildVersion.isBlank() || event.intent.isBlank() || event.stage.isBlank())
                    issues += "missing experiment metadata: $session"
            }
            if (rows.distinct().zipWithNext().any { (a,b) -> b.elapsedMs<a.elapsedMs }) issues += "nonmonotonic event time: $session"
            if (rows.none { it.type=="session_exit" }) issues += "session has no export closure: $session"
            if (rows.map { listOf(it.condition, it.scene, it.configurationId, it.buildVersion) }.distinct().size != 1)
                issues += "inconsistent session metadata: $session"
        }
        val captureOwners = events.filter { it.type == "capture_accepted" }.groupBy { it.captureId }
        captureOwners.forEach { (id, rows) ->
            if (id.isNullOrBlank() || rows.map { ResearchRoundKey(it.sessionId, it.roundId) }.distinct().size != 1)
                issues += "missing/reused capture identity"
        }
        events.filter { it.type in setOf("original_published", "save_retry") ||
            it.type == "save" && it.result in setOf("failed_retryable", "capture_failed", "burst_failed_retryable", "burst_failed") }
            .forEach { event ->
                val owner = captureOwners[event.captureId]?.firstOrNull()
                if (owner == null || owner.sessionId != event.sessionId || owner.roundId != event.roundId || owner.elapsedMs > event.elapsedMs)
                    issues += "unlinked capture event: ${event.type}"
            }
        return ResearchLogImport(events, issues.distinct())
    }
}

enum class BlindChoice { FIRST, SECOND, TIE, MISSING }
data class RegisteredTrial(val key: ResearchRoundKey, val participant: String, val condition: String, val scene: String, val order: Int)
data class BlindRating(val pairId: String, val first: ResearchRoundKey, val second: ResearchRoundKey,
    val audience: String, val choice: BlindChoice, val imagesPresent: Boolean = true)
data class RegisteredBlindPair(val pairId:String,val first:ResearchRoundKey,val second:ResearchRoundKey)
data class FollowUp(val participant: String, val observedDay: Int?, val realCapture: Boolean, val scheduledByResearch: Boolean)
data class ResearchSideSummary(val participants: Int, val trials: Int, val ratings: Int, val ties: Int,
    val missingRatings: Int, val missingImages: Int, val disagreements: Int, val lostFollowUps: Int,
    val naturalReturns: Int?, val issues: List<String>, val blockers: List<String>)

/** Mandatory pre-sampling decisions are supplied by the research owner, never chosen after seeing data. */
data class ResearchFreeze(val version: String, val exitRule: String, val blindAggregation: String,
    val missingAndTieRule: String, val incrementBaseline: String, val pairedTimeFailureRule: String,
    val d7Start: Int, val d7End: Int, val d7Denominator: String, val associationRule: String,
    val reliabilityPlan: String, val cueThresholds: String, val staticAnalysisLoad: String,
    val exportOwner: String, val accessAndRetention: String)

object ResearchSideContract {
    fun inspect(trials: List<RegisteredTrial>, ratings: List<BlindRating>, followUps: List<FollowUp>,
        freeze: ResearchFreeze? = null, pairs:List<RegisteredBlindPair>?=null): ResearchSideSummary {
        val issues = mutableListOf<String>()
        val participants = trials.map { it.participant }.toSet()
        if (trials.any { it.participant.isBlank() || it.order < 1 || it.condition !in setOf("SYSTEM", "STATIC", "DYNAMIC") ||
                it.scene !in setOf("WINDOW", "SCENERY", "BACKLIGHT") }) issues += "invalid registration"
        if (trials.map { it.key }.distinct().size != trials.size) issues += "duplicate trial registration"
        if (trials.groupBy { Triple(it.participant, it.scene, it.order) }.any { it.value.size > 1 }) issues += "duplicate randomized order"
        val registered = trials.associateBy { it.key }
        ratings.forEach { rating ->
            val a = registered[rating.first]; val b = registered[rating.second]
            if (a == null || b == null || a.key == b.key || a.participant != b.participant || a.scene != b.scene || a.condition == b.condition)
                issues += "invalid paired association"
            if (rating.audience !in setOf("shooter", "subject")) issues += "invalid rating audience"
        }
        if (ratings.map { it.pairId to it.audience }.distinct().size != ratings.size) issues += "duplicate rating"
        if (ratings.groupBy { it.pairId }.any { (_, group) -> group.map { it.first to it.second }.distinct().size != 1 })
            issues += "inconsistent randomized pair positions"
        if (followUps.map { it.participant }.distinct().size != followUps.size) issues += "duplicate follow-up"
        if (followUps.any { it.participant !in participants || (it.observedDay ?: 0) < 0 }) issues += "invalid follow-up association"
        val blockers = mutableListOf<String>()
        if(pairs==null)blockers+="pre-registered blind pair roster not supplied"
        else {
            if(pairs.map {it.pairId}.distinct().size!=pairs.size)issues+="duplicate registered pair"
            if(pairs.any {p->val a=registered[p.first];val b=registered[p.second]
                    p.pairId.isBlank() || a==null || b==null || a.participant!=b.participant || a.scene!=b.scene || a.condition==b.condition})
                issues+="invalid registered pair association"
            if(ratings.any {r->pairs.none {it.pairId==r.pairId && it.first==r.first && it.second==r.second}})issues+="unregistered rating pair"
        }
        if (freeze == null) blockers += "research rules/ownership/retention not frozen"
        else {
            if (listOf(freeze.version, freeze.exitRule, freeze.blindAggregation, freeze.missingAndTieRule,
                    freeze.incrementBaseline, freeze.pairedTimeFailureRule, freeze.d7Denominator, freeze.associationRule,
                    freeze.reliabilityPlan, freeze.cueThresholds, freeze.staticAnalysisLoad, freeze.exportOwner,
                    freeze.accessAndRetention).any { it.isBlank() } || freeze.d7Start < 0 || freeze.d7End < freeze.d7Start)
                blockers += "incomplete frozen configuration"
        }
        val validFollow = followUps.filter { it.participant in participants }
        val slots=completeRatings(ratings,pairs ?: ratings.distinctBy {it.pairId}.map {RegisteredBlindPair(it.pairId,it.first,it.second)})
        return ResearchSideSummary(participants.size, trials.size, ratings.size,
            ratings.count { it.choice == BlindChoice.TIE }, slots.count { it.choice == BlindChoice.MISSING },
            ratings.count { !it.imagesPresent }, ratings.groupBy { it.pairId }.count { (_, group) ->
                group.map { it.choice }.filter { it in setOf(BlindChoice.FIRST, BlindChoice.SECOND) }.distinct().size > 1 },
            participants.count { p -> validFollow.none { it.participant == p && it.observedDay != null } },
            if (blockers.isEmpty() && issues.isEmpty()) validFollow.count { it.realCapture && !it.scheduledByResearch &&
                it.observedDay != null && it.observedDay in freeze!!.d7Start..freeze.d7End } else null,
            issues.distinct(), blockers)
    }
    fun completeRatings(ratings:List<BlindRating>,pairs:List<RegisteredBlindPair>):List<BlindRating> =
        pairs.flatMap {p->listOf("shooter","subject").map {role-> ratings.firstOrNull {it.pairId==p.pairId && it.audience==role}
            ?: BlindRating(p.pairId,p.first,p.second,role,BlindChoice.MISSING)}}
}
