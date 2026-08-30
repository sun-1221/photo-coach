package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PhotoQualityScorerTest {
    @Test
    fun `sharp patterned frame outranks flat frame with same exposure`() {
        val flat = frame { _, _ -> 128 }
        val patterned = frame { x, y -> if ((x + y) % 2 == 0) 96 else 160 }
        val flatScore = PhotoQualityScorer.score(flat)
        val patternedScore = PhotoQualityScorer.score(patterned)
        assertTrue(patternedScore.sharpness > flatScore.sharpness)
        assertTrue(patternedScore.total > flatScore.total)
    }

    @Test
    fun `middle exposure outranks clipped exposure and scoring is deterministic`() {
        val middle = frame { _, _ -> 128 }
        val clipped = frame { x, _ -> if (x % 2 == 0) 0 else 255 }
        val first = PhotoQualityScorer.score(middle)
        val second = PhotoQualityScorer.score(middle)
        assertEquals(first, second)
        assertTrue(first.exposure > PhotoQualityScorer.score(clipped).exposure)
    }

    private fun frame(value: (Int, Int) -> Int): LuminanceFrame {
        val width = 8
        val height = 8
        return LuminanceFrame(width, height, IntArray(width * height) { index -> value(index % width, index / width) })
    }
}
