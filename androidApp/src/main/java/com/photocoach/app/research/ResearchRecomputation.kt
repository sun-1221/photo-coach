package com.photocoach.app.research

data class ResearchRoundKey(val sessionId: String, val roundId: Int)
enum class ResearchRoundOutcome { SUCCESS, FAILED, EXITED, INCOMPLETE }
data class RecomputedCapture(val captureId:String,val acceptedAtMs:Long,val publishedAtMs:Long?,
    val captureFailed:Boolean,val saveFailed:Boolean,val recovered:Boolean) {
    val firstAttemptSucceeded:Boolean get()=publishedAtMs!=null && !captureFailed && !saveFailed
}
data class RecomputedRound(val key: ResearchRoundKey, val outcome: ResearchRoundOutcome,
    val captureLatencyMs: Long?, val duplicateEvents: Int, val note: String,
    val captures: List<RecomputedCapture> = emptyList(), val exitAtMs:Long? = null,
    val exitedBeforePublication:Boolean = exitAtMs!=null &&
        (captures.mapNotNull {it.publishedAtMs}.minOrNull()?.let {exitAtMs<it} ?: true)) {
    val firstAttemptFailed:Boolean get()=captures.any {it.captureFailed || it.saveFailed}
    val recovered:Boolean get()=captures.any {it.recovered}
    val acceptedCaptures:Int get()=captures.size
}

/** Engineering replay only. Expected trials and threshold are supplied, never inferred from surviving successes. */
object ResearchRecomputation {
    fun replay(log: ResearchLogImport, expected: Set<ResearchRoundKey>, deadlineMs: Long,
        recoveryReceipts:List<ResearchRecoveryReceipt> = emptyList()): List<RecomputedRound> {
        if (!log.complete) return expected.map { RecomputedRound(it, ResearchRoundOutcome.INCOMPLETE, null, 0,
            log.issues.joinToString("; ")) }
        val unregistered = log.events.filter { it.type == "round_operable" }.any { ResearchRoundKey(it.sessionId, it.roundId) !in expected }
        if (unregistered) return expected.map { RecomputedRound(it, ResearchRoundOutcome.INCOMPLETE, null, 0, "unregistered trial") }
        val recovery=ResearchRecoveryAssociation.join(log.events,recoveryReceipts)
        if(recovery.issues.isNotEmpty()) return expected.map {RecomputedRound(it,ResearchRoundOutcome.INCOMPLETE,null,0,recovery.issues.joinToString("; "))}
        return replay(log.events, expected, deadlineMs).map {round -> round.copy(captures=round.captures.map {capture ->
            capture.copy(recovered=capture.recovered || capture.captureId in recovery.recoveredCaptureIds)
        })}
    }
    fun replay(events: List<ResearchEvent>, expected: Set<ResearchRoundKey>, deadlineMs: Long): List<RecomputedRound> {
        require(deadlineMs > 0)
        return expected.map { key ->
            val raw = events.filter { it.sessionId == key.sessionId && it.roundId == key.roundId }
            val rows = raw.distinct().sortedWith(compareBy<ResearchEvent> { it.elapsedMs }.thenBy { it.sequence ?: Long.MAX_VALUE })
            val duplicates = raw.size - rows.size
            val failed = rows.any { it.type == "save" && it.result in setOf("failed_retryable", "capture_failed", "burst_failed", "burst_failed_retryable") }
            val exit=rows.firstOrNull {it.type=="session_exit"}?.elapsedMs
            val captures=rows.filter {it.type=="capture_accepted" && !it.captureId.isNullOrBlank()}.distinctBy {it.captureId}.map {accepted ->
                val own=rows.drop(rows.indexOf(accepted)).filter {it.captureId==accepted.captureId}
                val publicationIndex=own.indexOfFirst {it.type=="original_published"}
                val published=own.getOrNull(publicationIndex)?.elapsedMs
                val before=if(publicationIndex<0)own else own.take(publicationIndex)
                val captureFailed=before.any {it.type=="save" && it.result in setOf("capture_failed","burst_failed")}
                val saveFailure=before.firstOrNull {it.type=="save" && it.result in setOf("failed_retryable","burst_failed_retryable")}
                val retry=saveFailure!=null && before.drop(before.indexOf(saveFailure)+1).any {it.type=="save_retry"}
                RecomputedCapture(accepted.captureId!!,accepted.elapsedMs,published,captureFailed,saveFailure!=null,
                    !captureFailed && saveFailure!=null && retry && published!=null)
            }
            fun result(outcome: ResearchRoundOutcome, latency: Long? = null, note: String) =
                RecomputedRound(key, outcome, latency, duplicates, note, captures,exit,
                    exit!=null && (rows.indexOfFirst {it.type=="original_published"}.let {it<0 || rows.indexOfFirst {it.type=="session_exit"}<it}))
            val start = rows.singleOrNull { it.type == "round_operable" }
            val metadata = rows.map { listOf(it.condition, it.scene, it.configurationId, it.buildVersion) }.distinct()
            if (start == null || metadata.size != 1 || start.condition !in setOf("STATIC", "DYNAMIC") || start.configurationId.isBlank()) {
                result(ResearchRoundOutcome.INCOMPLETE, note = "missing start or inconsistent experiment metadata")
            } else {
                val accepted = rows.filter { it.type == "capture_accepted" && it.elapsedMs >= start.elapsedMs }
                    .mapNotNull { it.captureId }.toSet()
                val published = rows.firstOrNull { it.type == "original_published" && it.captureId in accepted && it.elapsedMs >= start.elapsedMs }
                val latency = published?.let { it.elapsedMs - start.elapsedMs }
                val beforePublication=if(published==null)rows else rows.take(rows.indexOf(published))
                val failureBeforePublication=beforePublication.any {it.type in setOf("capture_rejected","round_timeout","save_failure_abandoned") ||
                    it.type=="save" && it.result in setOf("failed_retryable","capture_failed","burst_failed","burst_failed_retryable")}
                val exitIndex=rows.indexOfFirst {it.type=="session_exit"}
                val exitBeforePublication=exitIndex>=0 && (published==null || exitIndex<rows.indexOf(published))
                val failureBeforeExit=exitIndex>=0 && rows.take(exitIndex).any {
                    it.type in setOf("round_timeout","save_failure_abandoned","capture_rejected") ||
                        it.type=="save" && it.result in setOf("failed_retryable","capture_failed","burst_failed","burst_failed_retryable")
                }
                when {
                    failureBeforePublication -> result(ResearchRoundOutcome.FAILED,latency,
                        note="capture/save failure retained; subsequent original publication belongs to saving evidence")
                    exitBeforePublication &&
                        (failureBeforeExit || requireNotNull(exit)-start.elapsedMs>=deadlineMs) ->
                        result(ResearchRoundOutcome.FAILED,latency,note="failure or deadline preceded exit; retain denominator")
                    exitBeforePublication ->
                        result(ResearchRoundOutcome.EXITED,latency,note="exit retained independently of late publication")
                    latency != null -> result(if (latency <= deadlineMs) ResearchRoundOutcome.SUCCESS else ResearchRoundOutcome.FAILED,
                        latency, "verified original linked to accepted capture")
                    rows.any { it.type == "session_exit" } -> result(ResearchRoundOutcome.EXITED, note = "exit separately reported; exclusion rule not inferred")
                    failed || rows.any { it.type == "round_timeout" || it.type == "save_failure_abandoned" || it.type=="capture_rejected" } ->
                        result(ResearchRoundOutcome.FAILED, note = "explicit terminal event without original publication")
                    else -> result(ResearchRoundOutcome.INCOMPLETE, note = "missing terminal evidence; do not exclude from denominator")
                }
            }
        }
    }
}
