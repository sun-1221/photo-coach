package com.photocoach.coach

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PoseGuidanceTest {
    @Test fun `catalog covers six types and has bounded neutral cues`() {
        assertEquals(PoseCategory.entries.toSet(), PoseCueCatalog.cues.map { it.category }.toSet())
        assertTrue(PoseCueCatalog.validationErrors().isEmpty())
        val forbidden = listOf("开心", "紧张", "疲惫", "年龄", "性别", "漂亮", "肤色保护")
        assertTrue(PoseCueCatalog.cues.none { cue -> forbidden.any(cue.text::contains) })
    }

    @Test fun `observable cue requires stable entry and stable improvement`() {
        val reducer = PoseGuidanceReducer()
        reducer.select(PoseCategory.CLOSE_UP, 0)
        val away = Signals(faceCount = 1, faceReliable = true, faceTurnedAway = true)
        assertTrue(reducer.update(away, 0) is PoseGuidanceState.Acquiring)
        assertTrue(reducer.update(away, 600) is PoseGuidanceState.Eligible)
        assertTrue(reducer.update(away, 601) is PoseGuidanceState.CueActive)
        val improved = away.copy(faceTurnedAway = false)
        assertTrue(reducer.update(improved, 700) is PoseGuidanceState.CueActive)
        assertTrue(reducer.update(improved, 1_200) is PoseGuidanceState.Satisfied)
        assertTrue(reducer.update(improved, 1_201) is PoseGuidanceState.Cooldown)
    }

    @Test fun `low confidence and multiple people suppress single person pose`() {
        val reducer = PoseGuidanceReducer()
        reducer.select(PoseCategory.HALF_BODY, 0)
        assertTrue(reducer.update(Signals(faceCount = 0), 10) is PoseGuidanceState.LowConfidence)
        assertTrue(reducer.update(Signals(faceCount = 2, poseReliable = true), 20) is PoseGuidanceState.MultiPersonSuppressed)
        assertTrue(reducer.update(Signals(faceCount = 1, poseReliable = true), 30) is PoseGuidanceState.Acquiring)
    }

    @Test fun `inspiration cues never claim visual auto completion`() {
        val inspirations = PoseCueCatalog.cues.filter { it.completion == PoseCompletion.TIMED_INSPIRATION }
        assertTrue(inspirations.isNotEmpty())
        assertTrue(inspirations.all { !it.isSatisfied(Signals(faceCount = 1, faceReliable = true, poseReliable = true)) })
    }

    @Test fun `timed inspiration remains active until its timeout`() {
        val reducer = PoseGuidanceReducer()
        val evidence = Signals(faceCount = 1, faceReliable = true, poseReliable = true)
        reducer.select(PoseCategory.SOLO_INTERACTION, 0)
        assertTrue(reducer.update(evidence, 0) is PoseGuidanceState.Acquiring)
        assertTrue(reducer.update(evidence, 600) is PoseGuidanceState.Eligible)
        assertTrue(reducer.update(evidence, 601) is PoseGuidanceState.CueActive)
        assertTrue(reducer.update(evidence, 4_600) is PoseGuidanceState.CueActive)
        assertTrue(reducer.update(evidence, 4_601) is PoseGuidanceState.Satisfied)
    }

    @Test fun `changing eligible evidence restarts acquisition stability window`() {
        val reducer = PoseGuidanceReducer()
        reducer.select(PoseCategory.CLOSE_UP, 0)
        val turned = Signals(faceCount = 1, faceReliable = true, faceTurnedAway = true)
        val closedEyes = Signals(faceCount = 1, faceReliable = true, eyesLikelyClosed = true)
        assertTrue(reducer.update(turned, 0) is PoseGuidanceState.Acquiring)
        assertTrue(reducer.update(closedEyes, 599) is PoseGuidanceState.Acquiring)
        assertTrue(reducer.update(closedEyes, 600) is PoseGuidanceState.Acquiring)
        assertTrue(reducer.update(closedEyes, 1_199) is PoseGuidanceState.Eligible)
    }
}
