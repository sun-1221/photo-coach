package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DisplayRollTest {
    @Test fun uprightInEveryDisplayRotationIsLevel() {
        listOf(0f to 9.8f, 9.8f to 0f, 0f to -9.8f, -9.8f to 0f).forEachIndexed { rotation, (x, y) ->
            assertEquals(0f, displayRoll(x, y, 0f, rotation), 0.001f)
        }
    }
    @Test fun flatAndInvalidGravityAreUnknown() {
        assertTrue(displayRoll(0f, 0f, 9.8f, 0).isNaN())
        assertTrue(displayRoll(Float.NaN, 9f, 0f, 0).isNaN())
    }
}
