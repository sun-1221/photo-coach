package com.photocoach.coach

import kotlin.math.abs

enum class RequiredStep(val number: Int) {
    SHOOTER(1),
    SUBJECT(2),
}

sealed interface GuidanceStage {
    data object Initializing : GuidanceStage
    data class Observing(val sinceMs: Long) : GuidanceStage
    data class Action(
        val step: RequiredStep,
        val cue: Cue,
        val shownAtMs: Long,
        val displayNumber: Int = step.number,
        val resolvedSinceMs: Long? = null,
        val playbackFinishedAtMs: Long? = null,
        val playbackUnavailable: Boolean = false,
    ) : GuidanceStage
    data class Ready(
        val optionalAvailable: Boolean,
        val retainedSubjectCue: Cue? = null,
    ) : GuidanceStage
    data class Optional(
        val cue: Cue,
        val shownAtMs: Long,
        val playbackFinishedAtMs: Long? = null,
        val playbackUnavailable: Boolean = false,
    ) : GuidanceStage
    data object Capturing : GuidanceStage
    data class Saved(val photoRef: String?, val shownAtMs: Long) : GuidanceStage
    data class SaveFailed(val message: String, val retryAvailable: Boolean) : GuidanceStage
}

data class GuidanceSnapshot(
    val intent: ShotIntent,
    val intentLocked: Boolean,
    val stage: GuidanceStage,
    val shutterEnabled: Boolean,
    val optionalUsed: Boolean,
    val roundId: Int,
) {
    val currentCue: Cue?
        get() = when (stage) {
            is GuidanceStage.Action -> stage.cue
            is GuidanceStage.Ready -> stage.retainedSubjectCue
            is GuidanceStage.Optional -> stage.cue
            else -> null
        }

    val stepLabel: String?
        get() = (stage as? GuidanceStage.Action)?.let { "${it.displayNumber}/2" }

    val canSkip: Boolean
        get() = stage is GuidanceStage.Action || stage is GuidanceStage.Optional

    val canRequestOptional: Boolean
        get() = stage is GuidanceStage.Ready && stage.optionalAvailable && !optionalUsed
}

class GuidanceSession(
    initialIntent: ShotIntent = ShotIntent.CLOSE_UP,
) {
    private var intent = initialIntent
    private var intentLocked = false
    private var cameraAvailable = false
    private var stage: GuidanceStage = GuidanceStage.Initializing
    private var optionalUsed = false
    private var requiredStepsUsed = 0
    private var roundId = 0
    private var requiredSubject: Cue? = null
    private var optionalCandidates: List<Cue> = emptyList()
    private var optionalCandidateSignature: List<CueId> = emptyList()
    private var optionalCandidateSinceMs = 0L
    private var optionalCandidateFrames = 0
    private var candidateSignature: List<CueId> = emptyList()
    private var candidateSinceMs: Long = 0L
    private var candidateFrames: Int = 0
    private var baselineSignals: Signals = Signals()
    private var stableSubjectCue: Cue? = null
    private var stableShooterCue: Cue? = null
    private var shooterCandidateTrackingStarted = false
    private var shooterCandidateId: CueId? = null
    private var shooterCandidateSinceMs = 0L
    private var shooterCandidateFrames = 0
    private var subjectCandidateTrackingStarted = false
    private var subjectCandidateId: CueId? = null
    private var subjectCandidateSinceMs = 0L
    private var subjectCandidateFrames = 0
    private val presentedCueIds = mutableSetOf<CueId>()
    private val directionHistory = mutableMapOf<String, DirectionMark>()
    private var lastSpokenText: String? = null
    private var lastSpokenAtMs: Long = Long.MIN_VALUE

    fun snapshot(): GuidanceSnapshot = GuidanceSnapshot(
        intent = intent,
        intentLocked = intentLocked,
        stage = stage,
        shutterEnabled = cameraAvailable && stage !is GuidanceStage.Initializing &&
            stage !is GuidanceStage.Capturing && stage !is GuidanceStage.SaveFailed,
        optionalUsed = optionalUsed,
        roundId = roundId,
    )

    fun onCameraReady(nowMs: Long) {
        cameraAvailable = true
        if (stage is GuidanceStage.Initializing) beginObservation(nowMs, unlockIntent = false)
    }

    fun onCameraUnavailable() {
        cameraAvailable = false
    }

    fun selectIntent(selected: ShotIntent, nowMs: Long) {
        intent = selected
        intentLocked = true
        if (cameraAvailable) beginObservation(nowMs, unlockIntent = false)
    }

    fun autoSelectIntent(selected: ShotIntent): Boolean {
        if (intentLocked || stage !is GuidanceStage.Observing) return false
        if (intent == selected) return true
        intent = selected
        resetCandidateTracking()
        resetSubjectCandidateTracking()
        resetShooterCandidateTracking()
        return true
    }

    fun onCandidates(output: CoachOutput, signals: Signals, nowMs: Long) {
        if (output.intent != intent) return
        observeShooterCandidate(
            output.cues.filter { it.audience == Audience.SHOOTER }.maxByOrNull(Cue::priority),
            nowMs,
        )
        observeSubjectCandidate(
            output.cues.filter { it.audience == Audience.SUBJECT }.maxByOrNull(Cue::priority),
            nowMs,
        )
        when (val current = stage) {
            is GuidanceStage.Observing -> observeCandidates(output.cues, signals, current, nowMs)
            is GuidanceStage.Action -> updateAction(current, signals, nowMs)
            is GuidanceStage.Ready -> updateReady(current, output.cues, signals, nowMs)
            is GuidanceStage.Optional -> updateOptional(current, output.cues, nowMs)
            else -> Unit
        }
    }

    fun tick(nowMs: Long) {
        when (val current = stage) {
            is GuidanceStage.Observing -> {
                if (nowMs - current.sinceMs >= OBSERVATION_TIMEOUT_MS) enterReady()
            }
            is GuidanceStage.Action -> tickAction(current, nowMs)
            is GuidanceStage.Optional -> tickOptional(current, nowMs)
            is GuidanceStage.Saved -> {
                if (nowMs - current.shownAtMs >= SAVE_SUCCESS_DURATION_MS) {
                    beginObservation(nowMs, unlockIntent = true)
                }
            }
            else -> Unit
        }
    }

    fun skip(nowMs: Long): Boolean = when (val current = stage) {
        is GuidanceStage.Action -> {
            if (current.step == RequiredStep.SHOOTER) enterSubjectOrReady(nowMs) else enterReady()
            true
        }
        is GuidanceStage.Optional -> {
            enterReady()
            true
        }
        else -> false
    }

    fun requestOptional(nowMs: Long): Boolean {
        val ready = stage as? GuidanceStage.Ready ?: return false
        if (!ready.optionalAvailable || optionalUsed) return false
        val cue = optionalCandidates.firstOrNull { directionAllowed(it, nowMs) } ?: return false
        optionalUsed = true
        presentedCueIds += cue.id
        rememberDirection(cue, nowMs)
        stage = GuidanceStage.Optional(cue = cue, shownAtMs = nowMs)
        baselineSignals = baselineSignals.copy()
        return true
    }

    fun takeCueForSpeech(nowMs: Long): Cue? {
        val cue = when (val current = stage) {
            is GuidanceStage.Action -> current.cue
            is GuidanceStage.Optional -> current.cue
            else -> null
        } ?: return null
        if (cue.text == lastSpokenText && nowMs - lastSpokenAtMs < TTS_REPEAT_INTERVAL_MS) return null
        lastSpokenText = cue.text
        lastSpokenAtMs = nowMs
        return cue
    }

    fun onPlaybackFinished(nowMs: Long) {
        stage = when (val current = stage) {
            is GuidanceStage.Action -> current.copy(playbackFinishedAtMs = nowMs)
            is GuidanceStage.Optional -> current.copy(playbackFinishedAtMs = nowMs)
            else -> current
        }
    }

    fun onPlaybackStarting() {
        stage = when (val current = stage) {
            is GuidanceStage.Action -> current.copy(
                playbackFinishedAtMs = null,
                playbackUnavailable = false,
            )
            is GuidanceStage.Optional -> current.copy(
                playbackFinishedAtMs = null,
                playbackUnavailable = false,
            )
            else -> current
        }
    }

    fun onPlaybackUnavailable() {
        stage = when (val current = stage) {
            is GuidanceStage.Action -> current.copy(playbackUnavailable = true)
            is GuidanceStage.Optional -> current.copy(playbackUnavailable = true)
            else -> current
        }
    }

    fun onSubjectChannelsUnavailable() {
        stage = when (val current = stage) {
            is GuidanceStage.Action -> if (current.step == RequiredStep.SUBJECT) {
                readyStage(retainedSubjectCue = null)
            } else {
                current
            }
            is GuidanceStage.Optional -> if (current.cue.audience == Audience.SUBJECT) {
                readyStage(retainedSubjectCue = null)
            } else {
                current
            }
            else -> current
        }
    }

    fun clearRetainedSubjectCue() {
        val current = stage as? GuidanceStage.Ready ?: return
        if (current.retainedSubjectCue != null) stage = current.copy(retainedSubjectCue = null)
    }

    fun onShutter(nowMs: Long = 0L): Boolean {
        if (!snapshot().shutterEnabled) return false
        if (stage is GuidanceStage.Saved) beginObservation(nowMs, unlockIntent = true)
        stage = GuidanceStage.Capturing
        return true
    }

    fun onSaved(photoRef: String?, nowMs: Long) {
        stage = GuidanceStage.Saved(photoRef = photoRef, shownAtMs = nowMs)
    }

    fun onSaveFailed(message: String, retryAvailable: Boolean) {
        stage = GuidanceStage.SaveFailed(message = message, retryAvailable = retryAvailable)
    }

    fun onRetrySave(): Boolean {
        val failed = stage as? GuidanceStage.SaveFailed ?: return false
        if (!failed.retryAvailable || !cameraAvailable) return false
        stage = GuidanceStage.Capturing
        return true
    }

    fun abandonSaveFailure(nowMs: Long): Boolean {
        if (stage !is GuidanceStage.SaveFailed) return false
        beginObservation(nowMs, unlockIntent = true)
        return true
    }

    private fun observeCandidates(
        cues: List<Cue>,
        signals: Signals,
        observing: GuidanceStage.Observing,
        nowMs: Long,
    ) {
        val signature = cues.map(Cue::id)
        if (signature.isEmpty()) {
            resetCandidateTracking()
            if (nowMs - observing.sinceMs >= OBSERVATION_TIMEOUT_MS) enterReady()
            return
        }
        if (signature == candidateSignature) {
            candidateFrames += 1
        } else {
            candidateSignature = signature
            candidateSinceMs = nowMs
            candidateFrames = 1
        }
        val stable = nowMs - candidateSinceMs >= CANDIDATE_STABLE_MS || candidateFrames >= CANDIDATE_STABLE_FRAMES
        if (stable) startBudget(cues, signals, nowMs)
    }

    private fun startBudget(cues: List<Cue>, signals: Signals, nowMs: Long) {
        val shooter = cues.filter { it.audience == Audience.SHOOTER }.sortedByDescending(Cue::priority)
        val firstShooter = shooter.firstOrNull()
        requiredSubject = stableSubjectCue
        val subject = listOfNotNull(stableSubjectCue)
        optionalCandidates = (shooter.drop(1) + if (firstShooter == null) subject else emptyList())
            .sortedByDescending(Cue::priority)
        primeOptionalCandidateTracking(optionalCandidates, nowMs)
        baselineSignals = signals
        if (firstShooter == null) {
            requiredSubject = null
            val firstSubject = subject.firstOrNull()
            if (firstSubject != null && showRequiredCue(firstSubject, signals, nowMs)) return
            enterReady()
            return
        }
        showRequiredCue(firstShooter, signals, nowMs)
    }

    private fun updateAction(
        current: GuidanceStage.Action,
        signals: Signals,
        nowMs: Long,
    ) {
        if (current.step == RequiredStep.SUBJECT) {
            updateSubjectAction(current, nowMs)
            return
        }
        requiredSubject = stableSubjectCue
        val latest = stableShooterCue
        if (latest?.id != current.cue.id && nowMs - current.shownAtMs >= MIN_ACTION_DISPLAY_MS) {
            if (latest != null && directionAllowed(latest, nowMs)) {
                rememberDirection(latest, nowMs)
                presentedCueIds += latest.id
                baselineSignals = signals
                stage = current.copy(
                    cue = latest,
                    shownAtMs = nowMs,
                    resolvedSinceMs = null,
                    playbackFinishedAtMs = null,
                    playbackUnavailable = false,
                )
            } else {
                enterSubjectOrReady(nowMs)
            }
            return
        }
        val resolved = isResolved(current.cue, baselineSignals, signals)
        stage = when {
            resolved && current.resolvedSinceMs == null -> current.copy(resolvedSinceMs = nowMs)
            !resolved && current.resolvedSinceMs != null -> current.copy(resolvedSinceMs = null)
            else -> current
        }
    }

    private fun updateSubjectAction(current: GuidanceStage.Action, nowMs: Long) {
        val next = stableSubjectCue
        if (next?.id == current.cue.id) return
        if (nowMs - current.shownAtMs < MIN_ACTION_DISPLAY_MS) return
        if (next == null || !directionAllowed(next, nowMs)) {
            enterReady()
            return
        }
        presentedCueIds += next.id
        rememberDirection(next, nowMs)
        stage = current.copy(
            cue = next,
            shownAtMs = nowMs,
            resolvedSinceMs = null,
            playbackFinishedAtMs = null,
            playbackUnavailable = false,
        )
    }

    private fun updateReady(
        current: GuidanceStage.Ready,
        cues: List<Cue>,
        signals: Signals,
        nowMs: Long,
    ) {
        observeOptionalCandidates(cues, nowMs)
        val liveIds = cues.mapTo(mutableSetOf(), Cue::id)
        val persistentRecovery = stableShooterCue
            ?.takeIf { it.id in liveIds && it.id in PERSISTENT_RECOVERY_CUES }
            ?.takeIf { directionAllowed(it, nowMs) }
        if (persistentRecovery != null) {
            showPersistentRecoveryCue(persistentRecovery, signals, nowMs)
            return
        }
        if (requiredStepsUsed < MAX_REQUIRED_STEPS) {
            val nextRequired = listOfNotNull(stableShooterCue, stableSubjectCue)
                .filter { it.id in liveIds && it.id !in presentedCueIds }
                .filter(Cue::critical)
                .filter { directionAllowed(it, nowMs) }
                .maxByOrNull(Cue::priority)
            if (nextRequired != null && showRequiredCue(nextRequired, signals, nowMs)) return
        }
        val retained = current.retainedSubjectCue
        val keepRetained = retained != null && stableSubjectCue?.id == retained.id
        val next = current.copy(
            optionalAvailable = !optionalUsed && optionalCandidates.any { directionAllowed(it, nowMs) },
            retainedSubjectCue = if (keepRetained) retained else null,
        )
        if (next != current) stage = next
    }

    private fun updateOptional(current: GuidanceStage.Optional, cues: List<Cue>, nowMs: Long) {
        if (current.cue.audience != Audience.SUBJECT) return
        val next = stableSubjectCue
        if (next?.id == current.cue.id) return
        if (nowMs - current.shownAtMs < MIN_ACTION_DISPLAY_MS) return
        if (next == null || next.id in presentedCueIds || !directionAllowed(next, nowMs)) {
            refreshOptionalCandidates(cues)
            enterReady()
            return
        }
        presentedCueIds += next.id
        rememberDirection(next, nowMs)
        stage = GuidanceStage.Optional(cue = next, shownAtMs = nowMs)
    }

    private fun tickAction(current: GuidanceStage.Action, nowMs: Long) {
        if (current.step == RequiredStep.SHOOTER) {
            val shownLongEnough = nowMs - current.shownAtMs >= MIN_ACTION_DISPLAY_MS
            val improvedLongEnough = current.resolvedSinceMs?.let { nowMs - it >= ACTION_RESOLVED_STABLE_MS } == true
            val timedOut = current.cue.id !in PERSISTENT_RECOVERY_CUES &&
                nowMs - current.shownAtMs >= SHOOTER_TIMEOUT_MS
            if (shownLongEnough && improvedLongEnough || timedOut) {
                enterSubjectOrReady(nowMs)
            }
            return
        }
        val finished = current.playbackFinishedAtMs?.let { nowMs - it >= SUBJECT_AFTER_SPEECH_MS } == true
        val fallback = current.playbackUnavailable && nowMs - current.shownAtMs >= SUBJECT_FALLBACK_MS
        if (finished || fallback) enterReady(retainedSubjectCue = current.cue)
    }

    private fun tickOptional(current: GuidanceStage.Optional, nowMs: Long) {
        val done = if (current.cue.audience == Audience.SUBJECT) {
            current.playbackFinishedAtMs?.let { nowMs - it >= SUBJECT_AFTER_SPEECH_MS } == true ||
                current.playbackUnavailable && nowMs - current.shownAtMs >= SUBJECT_FALLBACK_MS
        } else {
            nowMs - current.shownAtMs >= OPTIONAL_DISPLAY_MS
        }
        if (done) {
            enterReady(
                retainedSubjectCue = current.cue.takeIf { it.audience == Audience.SUBJECT },
            )
        }
    }

    private fun enterSubjectOrReady(nowMs: Long) {
        val subject = requiredSubject
        requiredSubject = null
        if (subject == null || !directionAllowed(subject, nowMs) || !showRequiredCue(subject, baselineSignals, nowMs)) {
            enterReady()
        }
    }

    private fun showRequiredCue(cue: Cue, signals: Signals, nowMs: Long): Boolean {
        if (requiredStepsUsed >= MAX_REQUIRED_STEPS || cue.id in presentedCueIds) return false
        requiredStepsUsed += 1
        presentedCueIds += cue.id
        rememberDirection(cue, nowMs)
        baselineSignals = signals
        stage = GuidanceStage.Action(
            step = if (cue.audience == Audience.SUBJECT) RequiredStep.SUBJECT else RequiredStep.SHOOTER,
            cue = cue,
            shownAtMs = nowMs,
            displayNumber = requiredStepsUsed,
        )
        return true
    }

    private fun showPersistentRecoveryCue(cue: Cue, signals: Signals, nowMs: Long) {
        presentedCueIds += cue.id
        rememberDirection(cue, nowMs)
        baselineSignals = signals
        stage = GuidanceStage.Action(
            step = RequiredStep.SHOOTER,
            cue = cue,
            shownAtMs = nowMs,
            displayNumber = requiredStepsUsed.coerceIn(1, MAX_REQUIRED_STEPS),
        )
    }

    private fun enterReady(retainedSubjectCue: Cue? = null) {
        stage = readyStage(retainedSubjectCue)
    }

    private fun readyStage(retainedSubjectCue: Cue?): GuidanceStage.Ready =
        GuidanceStage.Ready(
            optionalAvailable = !optionalUsed && optionalCandidates.any { directionAllowed(it, Long.MAX_VALUE) },
            retainedSubjectCue = retainedSubjectCue,
        )

    private fun beginObservation(nowMs: Long, unlockIntent: Boolean) {
        if (unlockIntent) intentLocked = false
        roundId += 1
        stage = GuidanceStage.Observing(nowMs)
        optionalUsed = false
        requiredStepsUsed = 0
        requiredSubject = null
        optionalCandidates = emptyList()
        resetOptionalCandidateTracking()
        resetSubjectCandidateTracking()
        resetShooterCandidateTracking()
        presentedCueIds.clear()
        directionHistory.clear()
        resetCandidateTracking()
    }

    private fun observeSubjectCandidate(candidate: Cue?, nowMs: Long) {
        val id = candidate?.id
        if (!subjectCandidateTrackingStarted || id != subjectCandidateId) {
            subjectCandidateTrackingStarted = true
            subjectCandidateId = id
            subjectCandidateSinceMs = nowMs
            subjectCandidateFrames = 1
            return
        }
        subjectCandidateFrames += 1
        val stable = nowMs - subjectCandidateSinceMs >= CANDIDATE_STABLE_MS ||
            subjectCandidateFrames >= CANDIDATE_STABLE_FRAMES
        if (stable) stableSubjectCue = candidate
    }

    private fun observeShooterCandidate(candidate: Cue?, nowMs: Long) {
        val id = candidate?.id
        if (!shooterCandidateTrackingStarted || id != shooterCandidateId) {
            shooterCandidateTrackingStarted = true
            shooterCandidateId = id
            shooterCandidateSinceMs = nowMs
            shooterCandidateFrames = 1
            return
        }
        shooterCandidateFrames += 1
        val stable = nowMs - shooterCandidateSinceMs >= CANDIDATE_STABLE_MS ||
            shooterCandidateFrames >= CANDIDATE_STABLE_FRAMES
        if (stable) stableShooterCue = candidate
    }

    private fun refreshOptionalCandidates(cues: List<Cue>) {
        optionalCandidates = buildOptionalCandidates(cues)
        primeOptionalCandidateTracking(optionalCandidates, 0L)
    }

    private fun observeOptionalCandidates(cues: List<Cue>, nowMs: Long) {
        val live = buildOptionalCandidates(cues)
        val stableAdditions = listOfNotNull(stableShooterCue, stableSubjectCue)
            .filterNot { it.id in presentedCueIds }
            .filter { candidate -> optionalCandidates.none { it.id == candidate.id } }
        if (stableAdditions.isNotEmpty()) {
            optionalCandidates = (optionalCandidates + stableAdditions)
                .sortedByDescending(Cue::priority)
                .distinctBy(Cue::id)
        }
        val signature = live.map(Cue::id)
        if (signature != optionalCandidateSignature) {
            optionalCandidateSignature = signature
            optionalCandidateSinceMs = nowMs
            optionalCandidateFrames = 1
            return
        }
        optionalCandidateFrames += 1
        val stable = nowMs - optionalCandidateSinceMs >= CANDIDATE_STABLE_MS ||
            optionalCandidateFrames >= CANDIDATE_STABLE_FRAMES
        if (stable) optionalCandidates = live
    }

    private fun buildOptionalCandidates(cues: List<Cue>): List<Cue> = cues
            .filter { it.audience == Audience.SHOOTER }
            .plus(listOfNotNull(stableSubjectCue))
            .filterNot { it.id in presentedCueIds }
            .sortedByDescending(Cue::priority)
            .distinctBy(Cue::id)

    private fun primeOptionalCandidateTracking(candidates: List<Cue>, nowMs: Long) {
        optionalCandidateSignature = candidates.map(Cue::id)
        optionalCandidateSinceMs = nowMs
        optionalCandidateFrames = 0
    }

    private fun resetOptionalCandidateTracking() {
        optionalCandidateSignature = emptyList()
        optionalCandidateSinceMs = 0L
        optionalCandidateFrames = 0
    }

    private fun resetSubjectCandidateTracking() {
        stableSubjectCue = null
        subjectCandidateTrackingStarted = false
        subjectCandidateId = null
        subjectCandidateSinceMs = 0L
        subjectCandidateFrames = 0
    }

    private fun resetShooterCandidateTracking() {
        stableShooterCue = null
        shooterCandidateTrackingStarted = false
        shooterCandidateId = null
        shooterCandidateSinceMs = 0L
        shooterCandidateFrames = 0
    }

    private fun resetCandidateTracking() {
        candidateSignature = emptyList()
        candidateSinceMs = 0L
        candidateFrames = 0
    }

    private fun isResolved(cue: Cue, baseline: Signals, current: Signals): Boolean = when (cue.id) {
        CueId.FIND_PERSON -> current.faceCount == 1 || current.poseAvailable
        CueId.CLEAN_LENS -> !current.lensObscured
        CueId.KEEP_SUBJECT_IN_FRAME -> !current.subjectCutOff
        CueId.FOCUS_FACE -> current.focusOnFace && !baseline.focusOnFace
        CueId.MOVE_CLOSER -> current.faceRatio >= CueSelector.CLOSE_UP_MIN_FACE_RATIO ||
            current.faceRatio >= baseline.faceRatio + FACE_RATIO_IMPROVEMENT
        CueId.MOVE_CLOSER_KEEP_SCENERY -> current.faceRatio >= CueSelector.SCENERY_MIN_FACE_RATIO ||
            current.faceRatio >= baseline.faceRatio + FACE_RATIO_IMPROVEMENT
        CueId.LEVEL_PHONE -> abs(current.tiltDegrees) <= CueSelector.TILT_THRESHOLD
        CueId.PLACE_ON_THIRDS -> !current.personCentered
        CueId.PLACE_FACE_ON_UPPER_THIRD -> !current.faceTooLowInFrame
        CueId.LOWER_EXPOSURE -> !current.skyOverexposed
        else -> false
    }

    private fun directionAllowed(cue: Cue, nowMs: Long): Boolean {
        val group = cue.directionGroup ?: return true
        val previous = directionHistory[group] ?: return true
        if (cue.direction == 0 || previous.direction == 0 || cue.direction == previous.direction) return true
        return nowMs - previous.atMs >= DIRECTION_CONFLICT_INTERVAL_MS
    }

    private fun rememberDirection(cue: Cue, nowMs: Long) {
        val group = cue.directionGroup ?: return
        directionHistory[group] = DirectionMark(cue.direction, nowMs)
    }

    private data class DirectionMark(val direction: Int, val atMs: Long)

    companion object {
        const val CANDIDATE_STABLE_MS = 600L
        const val CANDIDATE_STABLE_FRAMES = 3
        const val ACTION_RESOLVED_STABLE_MS = 500L
        const val MIN_ACTION_DISPLAY_MS = 1_500L
        const val OBSERVATION_TIMEOUT_MS = 1_500L
        const val SHOOTER_TIMEOUT_MS = 8_000L
        const val SUBJECT_FALLBACK_MS = 2_500L
        const val SUBJECT_AFTER_SPEECH_MS = 1_000L
        const val OPTIONAL_DISPLAY_MS = 2_500L
        const val TTS_REPEAT_INTERVAL_MS = 8_000L
        const val DIRECTION_CONFLICT_INTERVAL_MS = 5_000L
        const val SAVE_SUCCESS_DURATION_MS = 1_500L
        const val FACE_RATIO_IMPROVEMENT = 0.02f
        private const val MAX_REQUIRED_STEPS = 2
        private val PERSISTENT_RECOVERY_CUES = setOf(CueId.FIND_PERSON, CueId.CLEAN_LENS)
    }
}
