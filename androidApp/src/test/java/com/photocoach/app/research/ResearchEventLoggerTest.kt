package com.photocoach.app.research

import java.nio.file.Files
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResearchEventLoggerTest {
    @Test fun diskFailureAndSizeLimitAreIsolatedAndMarked() {
        val directory = Files.createTempDirectory("logger-failure").toFile()
        val logger = ResearchEventLogger(directory)
        val event = ResearchEvent("test", 1, 1, "close_up", 1, "ready", sessionId = "anonymous-round")
        assertFalse(logger.record(event))
        assertTrue(logger.failedWrites == 1L)
        val bounded = ResearchEventLogger(directory.resolve("bounded.jsonl"), maximumBytes = 1)
        assertFalse(bounded.record(event))
        assertFalse(directory.resolve("bounded.jsonl").exists())
    }
    @Test
    fun storesOnlyDeclaredNonImageFields() {
        val file = Files.createTempDirectory("photo-coach-events").resolve("events.jsonl").toFile()
        ResearchEventLogger(file).record(
            ResearchEvent(
                type = "shutter",
                occurredAtMs = 100,
                elapsedMs = 80,
                intent = "close_up",
                roundId = 2,
                stage = "action_1",
                cueId = "move_closer",
                result = "accepted",
            ),
        )

        val text = file.readText()
        assertTrue(text.contains("\"type\":\"shutter\""))
        assertTrue(text.contains("\"cueId\":\"move_closer\""))
        listOf("image", "bitmap", "frame", "faceRect", "landmark", "identity").forEach {
            assertFalse(text.contains(it, ignoreCase = true))
        }
    }
}
