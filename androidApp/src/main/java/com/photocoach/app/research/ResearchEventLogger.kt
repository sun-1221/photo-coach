package com.photocoach.app.research

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ResearchEvent(
    val type: String,
    val occurredAtMs: Long,
    val elapsedMs: Long,
    val intent: String,
    val roundId: Int,
    val stage: String,
    val cueId: String? = null,
    val result: String? = null,
    val sessionId: String = "unknown",
    val condition: String = "NONE", val scene: String = "", val configurationId: String = "", val buildVersion: String = "",
    val captureId: String? = null,
    val sequence: Long? = null,
    val previousFailedWrites: Long = 0,
    val previousDroppedEvents: Long = 0,
)

class ResearchEventLogger(
    private val file: File,
    private val maximumBytes: Long = 4L * 1024 * 1024,
) {
    @Volatile var failedWrites: Long = 0
        private set
    private var nextSequence = 0L

    @Synchronized
    fun record(event: ResearchEvent): Boolean = try {
        val line = Json.encodeToString(event.copy(sequence = nextSequence++, previousFailedWrites = failedWrites)) + "\n"
        check(line.length <= 16_384 && file.length() + line.toByteArray(Charsets.UTF_8).size <= maximumBytes)
        file.parentFile?.mkdirs()
        file.appendText(line, Charsets.UTF_8)
        true
    } catch (_: Exception) {
        failedWrites++
        false
    }

}
