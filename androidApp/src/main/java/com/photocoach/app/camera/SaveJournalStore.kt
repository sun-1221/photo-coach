package com.photocoach.app.camera

import com.photocoach.app.creative.CaptureId
import java.io.File
import com.photocoach.app.beauty.BeautyPreset
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SaveJournal(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val captureId: String,
    val sequence: Int,
    val takenAtMillis: Long,
    val sourcePath: String,
    val motionPath: String? = null,
    val packagedPath: String? = null,
    val displayName: String,
    val style: String,
    val exposureStops: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val fade: Float = 0f,
    val styleStrength: Float = 1f,
    val derivativeQuality: String = DerivativeQuality.FULL.name,
    val completedStages: Set<String> = emptySet(),
    val failedStage: String? = null,
    val error: String? = null,
    val pendingUri: String? = null,
    val originalUri: String? = null,
    val derivativeUri: String? = null,
    val derivativePendingUri: String? = null,
    val derivativePath: String? = null,
    val motionPhotoRequested: Boolean = false,
    val motionPhotoFallback: Boolean = false,
    val derivativeRequested: Boolean = false,
    val outputLength: Long? = null,
    val verifiedAssetStages: Set<String> = emptySet(),
    val stageRetryCounts: Map<String, Int> = emptyMap(),
    val beautyPreset: String = BeautyPreset.OFF.name,
    val beautyEngineVersion: Int = BeautyPreset.ENGINE_VERSION,
    val batchId: String? = null,
    val derivativeId: String? = null,
    val exportSourceUri: String? = null,
    val effectWasDownsampled: Boolean = false,
    val exportWarning: String? = null,
) {
    init {
        CaptureId(captureId)
        derivativeId?.let(::CaptureId)
        batchId?.let(::CaptureId)
        require(sequence in 1..99)
        require(schemaVersion in 1..CURRENT_SCHEMA_VERSION)
    }

    val key: String get() = "${captureId}_S${sequence.toString().padStart(2, '0')}" +
        (derivativeId?.let { "_D$it" } ?: "")

    companion object { const val CURRENT_SCHEMA_VERSION = 3 }
}

class SaveJournalStore(private val directory: File) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    internal fun transactionKey(captureId: String, sequence: Int): String =
        "${directory.absolutePath}/$captureId/$sequence"

    internal fun transactionKey(record: SaveJournal): String =
        transactionKey(record.captureId, record.sequence) + (record.derivativeId?.let { "/$it" } ?: "")

    internal fun read(record: SaveJournal): SaveJournal? = fileFor(record).takeIf(File::isFile)?.let {
        json.decodeFromString<SaveJournal>(it.readText(Charsets.UTF_8))
    }

    fun write(record: SaveJournal): File {
        check(directory.exists() || directory.mkdirs()) { "cannot create save journal directory" }
        val target = fileFor(record)
        val temporary = File(directory, "${target.name}.tmp")
        try {
            temporary.writeText(json.encodeToString(record), Charsets.UTF_8)
            atomicReplace(temporary, target)
            return target
        } catch (error: Throwable) {
            temporary.delete()
            throw error
        }
    }

    fun readAll(): List<SaveJournal> {
        if (!directory.isDirectory) return emptyList()
        directory.listFiles { file -> file.isFile && file.extension == "json" }
            ?.sortedBy(File::getName)
            .orEmpty()
            .mapNotNull { file -> runCatching { json.decodeFromString<SaveJournal>(file.readText(Charsets.UTF_8)) }.getOrNull() }
            .let { return it }
    }

    fun delete(record: SaveJournal) {
        fileFor(record).delete()
    }

    private fun fileFor(record: SaveJournal): File = File(directory, "${record.key}.json")

    private fun atomicReplace(temporary: File, target: File) {
        try {
            Files.move(
                temporary.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
