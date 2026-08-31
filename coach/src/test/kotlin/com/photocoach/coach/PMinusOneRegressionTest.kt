package com.photocoach.coach

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class PMinusOneRegressionTest {
    private val engine = CoachEngine.loadDefault()
    private val json = Json { ignoreUnknownKeys = true }

    @TestFactory
    fun pMinusOneFixtures(): List<DynamicTest> {
        val text = checkNotNull(javaClass.classLoader.getResourceAsStream("fixtures/p-minus-one-regression.json"))
            .bufferedReader()
            .readText()
        return json.decodeFromString<List<FixtureCase>>(text).map { case ->
            DynamicTest.dynamicTest(case.id) {
                val output = engine.evaluate(case.signals.toDomain(), case.intent)
                val joined = output.cues.joinToString(" | ") { it.text }
                case.expectScene?.let { assertEquals(it, output.sceneId, joined) }
                if (case.expectNoScene) assertNull(output.sceneId, joined)
                case.rejectScene?.let { assertFalse(output.sceneId == it, "should not match $it: $joined") }
                case.mustInclude.forEach { assertTrue(joined.contains(it), "missing [$it] in [$joined]") }
                case.mustExclude.forEach { assertFalse(joined.contains(it), "forbidden [$it] in [$joined]") }
                assertTrue(output.cues.size <= 3)
                assertTrue(output.startParams.flashOff)
            }
        }
    }
}

@Serializable
private data class FixtureCase(
    val id: String,
    val intent: ShotIntent,
    val signals: SignalFixture,
    val expectScene: SceneId? = null,
    val expectNoScene: Boolean = false,
    val rejectScene: SceneId? = null,
    val mustInclude: List<String> = emptyList(),
    val mustExclude: List<String> = emptyList(),
)

@Serializable
private data class SignalFixture(
    val faceCount: Int = 0,
    val faceRatio: Float = 0f,
    val faceDarkerThanScene: Boolean = false,
    val tiltDegrees: Float = 0f,
    val coarseScene: CoarseScene = CoarseScene.UNKNOWN,
    val poseAvailable: Boolean = false,
    val hasTelephotoPreset: Boolean = false,
    val oneSideBrighter: Boolean = false,
    val hasLargeEnvironment: Boolean = false,
    val personCentered: Boolean = false,
    val faceTooLowInFrame: Boolean = false,
    val focusOnFace: Boolean = true,
    val headTiltedBack: Boolean = false,
    val shouldersSquare: Boolean = false,
    val shouldersRaised: Boolean = false,
    val handsIdle: Boolean = false,
    val skyOverexposed: Boolean = false,
    val subjectCutOff: Boolean = false,
    val lensObscured: Boolean = false,
) {
    fun toDomain(): Signals = Signals(
        faceCount = faceCount,
        faceRatio = faceRatio,
        faceDarkerThanScene = faceDarkerThanScene,
        tiltDegrees = tiltDegrees,
        coarseScene = coarseScene,
        poseAvailable = poseAvailable,
        hasTelephotoPreset = hasTelephotoPreset,
        oneSideBrighter = oneSideBrighter,
        hasLargeEnvironment = hasLargeEnvironment,
        personCentered = personCentered,
        faceTooLowInFrame = faceTooLowInFrame,
        focusOnFace = focusOnFace,
        headTiltedBack = headTiltedBack,
        shouldersSquare = shouldersSquare,
        shouldersRaised = shouldersRaised,
        handsIdle = handsIdle,
        skyOverexposed = skyOverexposed,
        subjectCutOff = subjectCutOff,
        lensObscured = lensObscured,
    )
}
