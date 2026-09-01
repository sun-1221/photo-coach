package com.photocoach.coach

enum class PoseCategory { CLOSE_UP, HALF_BODY, FULL_BODY, SEATED, WALKING, SOLO_INTERACTION }
enum class PoseCompletion { OBSERVABLE, TIMED_INSPIRATION }

data class PoseCueDefinition(
    val id: String,
    val category: PoseCategory,
    val text: String,
    val audience: Audience = Audience.SUBJECT,
    val completion: PoseCompletion,
    val timeoutMs: Long,
    val cooldownGroup: String,
    val isEligible: (Signals) -> Boolean,
    val isSatisfied: (Signals) -> Boolean,
)

object PoseCueCatalog {
    val cues: List<PoseCueDefinition> = listOf(
        observable("close-face", PoseCategory.CLOSE_UP, "脸转回一点", "face-direction",
            { it.faceReliable && it.faceTurnedAway }, { it.faceReliable && !it.faceTurnedAway }),
        observable("close-eyes", PoseCategory.CLOSE_UP, "眼睛睁开，看镜头", "eyes",
            { it.faceReliable && !it.faceTurnedAway && it.eyesLikelyClosed },
            { it.faceReliable && !it.faceTurnedAway && !it.eyesLikelyClosed }),
        observable("half-shoulders", PoseCategory.HALF_BODY, "肩放松一点", "shoulders",
            { it.poseReliable && it.shouldersRaised }, { it.poseReliable && !it.shouldersRaised }),
        observable("half-hand", PoseCategory.HALF_BODY, "一只手放到身体外侧", "hands",
            { it.poseReliable && it.handsIdle }, { it.poseReliable && it.atLeastOneHandOutsideTorso }),
        observable("half-angle", PoseCategory.HALF_BODY, "身体侧一点", "body-angle",
            { it.poseReliable && it.shouldersSquare && it.faceReliable }, { it.poseReliable && !it.shouldersSquare }),
        observable("full-feet", PoseCategory.FULL_BODY, "脚下留一点空", "frame-edge",
            { it.poseReliable && it.anklesVisible && it.anklesNearBottomEdge },
            { it.poseReliable && it.anklesVisible && !it.anklesNearBottomEdge }),
        observable("full-joints", PoseCategory.FULL_BODY, "手肘和膝盖别贴边", "frame-edge",
            { it.poseReliable && it.jointsNearFrameEdge }, { it.poseReliable && !it.jointsNearFrameEdge }),
        inspiration("full-step", PoseCategory.FULL_BODY, "一只脚向前半步", "step-inspiration"),
        observable("seated-upright", PoseCategory.SEATED, "身体坐直，肩放松", "seated",
            { it.poseReliable && it.seatedCandidate && (!it.torsoUpright || it.shouldersRaised) },
            { it.poseReliable && it.seatedCandidate && it.torsoUpright && !it.shouldersRaised }),
        inspiration("seated-chair", PoseCategory.SEATED, "坐到椅子前半边", "seated-inspiration"),
        observable("walking-step", PoseCategory.WALKING, "慢慢走一步", "walking",
            { it.poseReliable && it.walkingCandidate }, { it.poseReliable && it.walkingMotionStable }),
        inspiration("solo-interaction", PoseCategory.SOLO_INTERACTION, "靠近环境，自然做一个动作", "interaction"),
    )

    fun forCategory(category: PoseCategory): List<PoseCueDefinition> = cues.filter { it.category == category }

    fun validationErrors(): List<String> = buildList {
        val duplicates = cues.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
        if (duplicates.isNotEmpty()) add("duplicate cue ids: ${duplicates.sorted().joinToString()}")
        PoseCategory.entries.filter { forCategory(it).isEmpty() }.forEach { add("missing category: $it") }
        cues.filter { it.text.isBlank() || it.text.length > 16 }.forEach { add("invalid text: ${it.id}") }
        cues.filter { it.timeoutMs <= 0L || it.cooldownGroup.isBlank() }.forEach { add("invalid timing: ${it.id}") }
    }

    private fun observable(id: String, category: PoseCategory, text: String, cooldownGroup: String,
        eligible: (Signals) -> Boolean, satisfied: (Signals) -> Boolean) =
        PoseCueDefinition(id, category, text, completion = PoseCompletion.OBSERVABLE, timeoutMs = 8_000L,
            cooldownGroup = cooldownGroup, isEligible = eligible, isSatisfied = satisfied)

    private fun inspiration(id: String, category: PoseCategory, text: String, cooldownGroup: String) =
        PoseCueDefinition(id, category, text, completion = PoseCompletion.TIMED_INSPIRATION, timeoutMs = 4_000L,
            cooldownGroup = cooldownGroup,
            isEligible = { it.faceCount <= 1 && (it.faceReliable || it.poseReliable) }, isSatisfied = { false })
}

sealed interface PoseGuidanceState {
    data object Disabled : PoseGuidanceState
    data class Acquiring(val category: PoseCategory, val sinceMs: Long) : PoseGuidanceState
    data class Eligible(val cue: PoseCueDefinition, val sinceMs: Long) : PoseGuidanceState
    data class CueActive(val cue: PoseCueDefinition, val sinceMs: Long) : PoseGuidanceState
    data class Satisfied(val cue: PoseCueDefinition, val atMs: Long) : PoseGuidanceState
    data class Cooldown(val category: PoseCategory, val group: String, val untilMs: Long) : PoseGuidanceState
    data class LowConfidence(val category: PoseCategory, val sinceMs: Long) : PoseGuidanceState
    data class MultiPersonSuppressed(val category: PoseCategory, val sinceMs: Long) : PoseGuidanceState
}

class PoseGuidanceReducer(
    private val acquisitionMs: Long = 600L,
    private val satisfactionMs: Long = 500L,
    private val cooldownMs: Long = 5_000L,
) {
    var state: PoseGuidanceState = PoseGuidanceState.Disabled
        private set
    private var evidenceSinceMs: Long? = null
    private var evidenceCueId: String? = null
    private var satisfactionSinceMs: Long? = null

    fun select(category: PoseCategory, nowMs: Long) { state = PoseGuidanceState.Acquiring(category, nowMs); reset() }
    fun disable() { state = PoseGuidanceState.Disabled; reset() }

    fun update(signals: Signals, nowMs: Long): PoseGuidanceState {
        val category = categoryOf(state) ?: return state
        if (signals.faceCount > 1) {
            state = PoseGuidanceState.MultiPersonSuppressed(category, nowMs); reset(); return state
        }
        if (!signals.faceReliable && !signals.poseReliable) {
            state = PoseGuidanceState.LowConfidence(category, nowMs); reset(); return state
        }
        when (val current = state) {
            is PoseGuidanceState.MultiPersonSuppressed, is PoseGuidanceState.LowConfidence ->
                state = PoseGuidanceState.Acquiring(category, nowMs)
            is PoseGuidanceState.Acquiring -> acquire(category, signals, nowMs)
            is PoseGuidanceState.Eligible -> state = PoseGuidanceState.CueActive(current.cue, nowMs)
            is PoseGuidanceState.CueActive -> updateActive(current, signals, nowMs)
            is PoseGuidanceState.Satisfied ->
                state = PoseGuidanceState.Cooldown(category, current.cue.cooldownGroup, nowMs + cooldownMs)
            is PoseGuidanceState.Cooldown -> if (nowMs >= current.untilMs) state = PoseGuidanceState.Acquiring(category, nowMs)
            PoseGuidanceState.Disabled -> Unit
        }
        return state
    }

    private fun acquire(category: PoseCategory, signals: Signals, nowMs: Long) {
        val cue = PoseCueCatalog.forCategory(category).firstOrNull { it.isEligible(signals) }
        if (cue == null) {
            evidenceSinceMs = null
            evidenceCueId = null
            return
        }
        if (evidenceCueId != cue.id) {
            evidenceCueId = cue.id
            evidenceSinceMs = nowMs
        }
        val since = evidenceSinceMs ?: nowMs.also { evidenceSinceMs = it }
        if (nowMs - since >= acquisitionMs) {
            state = PoseGuidanceState.Eligible(cue, nowMs)
            evidenceSinceMs = null
            evidenceCueId = null
        }
    }

    private fun updateActive(current: PoseGuidanceState.CueActive, signals: Signals, nowMs: Long) {
        val cue = current.cue
        if (nowMs - current.sinceMs >= cue.timeoutMs) {
            state = PoseGuidanceState.Satisfied(cue, nowMs); satisfactionSinceMs = null; return
        }
        if (!cue.isEligible(signals) && !cue.isSatisfied(signals)) {
            state = PoseGuidanceState.Acquiring(cue.category, nowMs); reset(); return
        }
        if (cue.isSatisfied(signals)) {
            val since = satisfactionSinceMs ?: nowMs.also { satisfactionSinceMs = it }
            if (nowMs - since >= satisfactionMs) state = PoseGuidanceState.Satisfied(cue, nowMs)
        } else satisfactionSinceMs = null
    }

    private fun categoryOf(value: PoseGuidanceState): PoseCategory? = when (value) {
        PoseGuidanceState.Disabled -> null
        is PoseGuidanceState.Acquiring -> value.category
        is PoseGuidanceState.Eligible -> value.cue.category
        is PoseGuidanceState.CueActive -> value.cue.category
        is PoseGuidanceState.Satisfied -> value.cue.category
        is PoseGuidanceState.Cooldown -> value.category
        is PoseGuidanceState.LowConfidence -> value.category
        is PoseGuidanceState.MultiPersonSuppressed -> value.category
    }

    private fun reset() { evidenceSinceMs = null; evidenceCueId = null; satisfactionSinceMs = null }
}
