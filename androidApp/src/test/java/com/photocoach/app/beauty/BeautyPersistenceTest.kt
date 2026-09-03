package com.photocoach.app.beauty

import com.photocoach.app.camera.SaveJournal
import com.photocoach.app.camera.SaveJournalStore
import com.photocoach.app.camera.SaveStage
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.EditRecipe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class BeautyPersistenceTest {
    @TempDir lateinit var directory: Path

    @Test fun oldRecipeAndJournalRemainOff() {
        val recipe = Json.decodeFromString<EditRecipe>("""{"schemaVersion":1,"captureId":"abcdef0123456789",
            "sequence":1,"takenAtMillis":123,"style":"ORIGINAL","exposureStops":0,"contrast":0,
            "saturation":0,"temperature":0,"tint":0,"fade":0,"styleStrength":1,"sourceIsMotionPhoto":false}""")
        val journal = Json.decodeFromString<SaveJournal>("""{"schemaVersion":2,"captureId":"abcdef0123456789",
            "sequence":1,"takenAtMillis":123,"sourcePath":"pending.jpg","displayName":"original.jpg","style":"ORIGINAL"}""")
        assertEquals("OFF",recipe.beautyPreset)
        assertEquals("OFF",journal.beautyPreset)
        assertEquals(1,recipe.beautyEngineVersion)
        assertEquals(1,journal.beautyEngineVersion)
    }

    @Test fun recipeOnlyPersistsTheVersionedPresetNotFacialData() {
        val recipe = EditRecipe.create(CaptureId("abcdef0123456789"),1,123,
            CreativeStyle.NATURAL_PORTRAIT,EditAdjustment(),false,BeautyPreset.SOFT)
        val encoded = Json { encodeDefaults = true }.encodeToString(recipe)
        assertEquals(recipe,Json.decodeFromString<EditRecipe>(encoded))
        assertEquals("SOFT",recipe.beautyPreset)
        assertFalse(encoded.contains("landmark",ignoreCase=true))
        assertFalse(encoded.contains("face",ignoreCase=true))
        assertFalse(encoded.contains("mask",ignoreCase=true))
    }

    @Test fun retryKeepsOriginalIdentityAndCapturedBeautyAndReplacesOneJournal() {
        val store = SaveJournalStore(directory.toFile())
        val original = SaveJournal(captureId="abcdef0123456789",sequence=1,takenAtMillis=123,
            sourcePath="pending.jpg",displayName="original.jpg",style="ORIGINAL",
            originalUri="content://media/external/images/media/7",beautyPreset="NATURAL",beautyEngineVersion=1,
            derivativeRequested=true,completedStages=setOf(SaveStage.ORIGINAL_PUBLISH.name))
        store.write(original)
        val failed = store.readAll().single().copy(failedStage=SaveStage.DERIVATIVE_GENERATE.name,
            stageRetryCounts=mapOf(SaveStage.DERIVATIVE_GENERATE.name to 1))
        store.write(failed)
        val restored = store.readAll().single()
        assertEquals(original.originalUri,restored.originalUri)
        assertEquals(original.captureId,restored.captureId)
        assertEquals(original.beautyPreset,restored.beautyPreset)
        assertEquals(original.beautyEngineVersion,restored.beautyEngineVersion)
        assertTrue(SaveStage.ORIGINAL_PUBLISH.name in restored.completedStages)
        assertEquals(1,directory.toFile().listFiles()!!.size)
    }
}
