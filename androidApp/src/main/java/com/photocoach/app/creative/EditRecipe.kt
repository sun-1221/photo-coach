package com.photocoach.app.creative

import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class EditRecipe(
    val schemaVersion: Int = 1,
    val captureId: String,
    val sequence: Int,
    val takenAtMillis: Long,
    val style: String,
    val exposureStops: Float,
    val contrast: Float,
    val saturation: Float,
    val temperature: Float,
    val tint: Float,
    val fade: Float,
    val styleStrength: Float,
    val sourceIsMotionPhoto: Boolean,
    val derivativeColorSpace: String = "sRGB SDR",
) {
    init {
        CaptureId(captureId)
        require(sequence in 1..99)
    }

    companion object {
        fun create(
            captureId: CaptureId,
            sequence: Int,
            takenAtMillis: Long,
            style: CreativeStyle,
            edit: EditAdjustment,
            sourceIsMotionPhoto: Boolean,
        ): EditRecipe {
            val safe = edit.normalized()
            return EditRecipe(
                captureId = captureId.value,
                sequence = sequence,
                takenAtMillis = takenAtMillis,
                style = style.name,
                exposureStops = safe.exposureStops,
                contrast = safe.contrast,
                saturation = safe.saturation,
                temperature = safe.temperature,
                tint = safe.tint,
                fade = safe.fade,
                styleStrength = safe.styleStrength,
                sourceIsMotionPhoto = sourceIsMotionPhoto,
            )
        }
    }
}

class EditRecipeStore(private val directory: File) {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    fun write(recipe: EditRecipe): File {
        check(directory.exists() || directory.mkdirs()) { "cannot create recipe directory" }
        val target = File(directory, CaptureIdentity.recipeFileName(CaptureId(recipe.captureId), recipe.sequence))
        val temporary = File(target.parentFile, "${target.name}.tmp")
        try {
            temporary.writeText(json.encodeToString(recipe), Charsets.UTF_8)
            atomicReplace(temporary, target)
            return target
        } catch (error: Throwable) {
            temporary.delete()
            throw error
        }
    }

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
