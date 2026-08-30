package com.photocoach.app.research

import java.io.File

data class ResearchEvent(
    val type: String,
    val occurredAtMs: Long,
    val elapsedMs: Long,
    val intent: String,
    val roundId: Int,
    val stage: String,
    val cueId: String? = null,
    val result: String? = null,
)

class ResearchEventLogger(
    private val file: File,
) {
    @Synchronized
    fun record(event: ResearchEvent) {
        file.parentFile?.mkdirs()
        file.appendText(event.toJsonLine() + "\n", Charsets.UTF_8)
    }

    private fun ResearchEvent.toJsonLine(): String = buildString {
        append('{')
        field("type", type)
        field("occurredAtMs", occurredAtMs)
        field("elapsedMs", elapsedMs)
        field("intent", intent)
        field("roundId", roundId)
        field("stage", stage)
        cueId?.let { field("cueId", it) }
        result?.let { field("result", it) }
        if (lastOrNull() == ',') deleteCharAt(lastIndex)
        append('}')
    }

    private fun StringBuilder.field(name: String, value: String) {
        append('"').append(name).append("\":\"").append(value.escapeJson()).append("\",")
    }

    private fun StringBuilder.field(name: String, value: Long) {
        append('"').append(name).append("\":").append(value).append(',')
    }

    private fun StringBuilder.field(name: String, value: Int) {
        append('"').append(name).append("\":").append(value).append(',')
    }

    private fun String.escapeJson(): String = buildString {
        this@escapeJson.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(char)
            }
        }
    }
}
