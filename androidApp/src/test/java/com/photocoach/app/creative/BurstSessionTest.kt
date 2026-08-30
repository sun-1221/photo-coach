package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BurstSessionTest {
    @Test
    fun `requires explicit enable rejects reentry and completes at exactly three`() {
        val session = BurstSession()
        assertFalse(session.start(userEnabledThreeShot = false))
        assertTrue(session.start(userEnabledThreeShot = true))
        assertFalse(session.start(userEnabledThreeShot = true))

        session.record(photo("first", total = 0.5, sharpness = 0.7, exposure = 0.4))
        session.record(photo("second", total = 0.8, sharpness = 0.8, exposure = 0.8))
        assertInstanceOf(BurstState.Capturing::class.java, session.state)
        session.record(photo("third", total = 0.6, sharpness = 0.9, exposure = 0.2))

        val complete = session.state as BurstState.Complete
        assertEquals(3, complete.photos.size)
        assertEquals("second", complete.recommendedId)
        session.record(photo("fourth", 1.0, 1.0, 1.0))
        assertEquals(3, (session.state as BurstState.Complete).photos.size)
    }

    @Test
    fun `failure retains completed photos and stable tie picks earlier sequence`() {
        val ties = BurstSession().also { it.start(true) }
        repeat(3) { index -> ties.record(photo("p$index", 0.5, 0.5, 0.5)) }
        assertEquals("p0", (ties.state as BurstState.Complete).recommendedId)

        val failed = BurstSession().also { it.start(true) }
        failed.record(photo("kept", 0.5, 0.5, 0.5))
        val state = failed.fail("storage failed") as BurstState.Failed
        assertEquals(listOf("kept"), state.completed.map(BurstPhoto::id))
        assertEquals("storage failed", state.message)
        assertTrue(failed.resumeAfterExplicitRetry())
        assertInstanceOf(BurstState.Capturing::class.java, failed.state)
    }

    private fun photo(id: String, total: Double, sharpness: Double, exposure: Double) = BurstPhoto(
        id = id,
        score = PhotoQualityScore(total, sharpness, exposure),
        sequence = 99,
    )
}
