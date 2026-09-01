package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PortraitToneGuardTest {
    private val before = FaceToneSample(120f, 28f, 42f, .01f)

    @Test fun `unknown and multiple faces never claim protection`() {
        assertEquals(PortraitToneDecision.Unknown,
            PortraitToneGuard.constrain(CreativeStyle.NATURAL_PORTRAIT, 1f, 2, false, null, null))
    }

    @Test fun `risky face change only lowers whole image strength`() {
        val decision = PortraitToneGuard.constrain(
            CreativeStyle.COOL_PORTRAIT, 1f, 1, true, before,
            FaceToneSample(92f, 55f, 18f, .12f),
        ) as PortraitToneDecision.Conservative
        assertTrue(decision.strength <= .35f)
        assertEquals("已使用人像保守强度", decision.reason)
    }

    @Test fun `even safe sample respects uncalibrated style range`() {
        val decision = PortraitToneGuard.constrain(
            CreativeStyle.NATURAL_PORTRAIT, 1f, 1, true, before,
            FaceToneSample(122f, 30f, 44f, .01f),
        ) as PortraitToneDecision.Conservative
        assertEquals(.65f, decision.strength)
    }
}
