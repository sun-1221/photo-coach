package com.photocoach.app.camera

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.creative.*
import java.io.File
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterruptedSaveRecoveryTest {
    @Test fun legacySchemasRecoverOriginalKeyAndNeverRepublishAfterRecipeFailure() {
        for (schema in 1..3) fixture { f ->
            f.recipes.writeText("block recipe directory")
            val legacy = f.record.copy(schemaVersion = schema, motionPhotoRequested = false, packagedPath = null,
                displayName = f.originalName, completedStages = setOf(SaveStage.SPACE_CHECK.name))
            f.journals.write(legacy)
            f.recovery().recover()
            val failed = f.journals.readAll().single()
            assertEquals(legacy.key, failed.key)
            assertNull(failed.batchId)
            val original = Uri.parse(requireNotNull(failed.originalUri)); f.published += original
            f.recovery().recover()
            assertEquals(original.toString(), f.journals.readAll().single().originalUri)
            assertEquals(listOf(original), f.rows())
            f.recipes.delete()
            f.recovery().recover()
            assertEquals(listOf(original), f.rows())
            assertTrue(f.journals.readAll().isEmpty())
        }
    }

    @Test fun completedExplicitExportOnlyVerifiesAndCleansOnRecoveryAndStaleRetry() = fixture { f ->
        val original = CaptureSaver.publish(f.resolver, f.source, f.originalName, f.time, motionPhoto = false)
        f.published += original
        val derivative = CaptureIdentity.create()
        val request = f.record.copy(motionPhotoRequested = false, packagedPath = null, sourcePath = "",
            completedStages = emptySet(), derivativeId = derivative.value, exportSourceUri = original.toString(),
            displayName = CaptureIdentity.displayName(derivative, CaptureAssetKind.EDITED, 1, f.time))
        val saved = f.recovery().createExport(request); f.published += saved.uri
        val complete = f.journals.read(request)!!
        assertNotNull(complete.derivativeUri)
        // A completed journal and stale request coexist after process restart.
        f.recovery().recover()
        val retried = f.recovery().recoverExport(request)
        assertEquals(saved.uri, retried.uri)
        assertArrayEquals(f.originalBytes, f.resolver.openInputStream(original)!!.use { it.readBytes() })
        assertEquals(SaveStage.COMPLETE.name, f.journals.read(request)!!.completedStages.last())
    }
    @Test fun corruptMotionFallsBackOnceAndSurvivesAnotherInterruptedStage() = fixture { f ->
        f.recipes.writeText("block recipe directory")
        f.journals.write(f.record)
        f.recovery().recover()
        val failed = f.journals.readAll().single()
        assertTrue("recovery journal: $failed", failed.motionPhotoFallback)
        assertNull(failed.packagedPath)
        assertEquals(SaveStage.RECIPE_WRITE.name, failed.failedStage)
        val uri = Uri.parse(requireNotNull(failed.originalUri))
        f.published += uri
        assertArrayEquals(f.originalBytes, f.resolver.openInputStream(uri)!!.use { it.readBytes() })
        f.recipes.delete()
        f.recovery().recover()
        f.recovery().recover()
        assertTrue(f.journals.readAll().isEmpty())
        assertEquals(listOf(uri), f.rows())
        assertFalse(f.packaged.exists())
        assertFalse(f.source.exists())
    }

    @Test fun corruptMotionPendingRowIsRetiredBeforeJpegFallback() = fixture { f ->
        val pending = f.resolver.insert(f.collection, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, f.record.displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, CaptureSaver.RELATIVE_DIR)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        })!!
        f.published += pending
        f.journals.write(f.record.copy(pendingUri = pending.toString()))
        f.recovery().recover()
        val rows = f.rows()
        f.published += rows
        assertEquals(1, rows.size)
        assertNotEquals(pending, rows.single())
        assertArrayEquals(f.originalBytes, f.resolver.openInputStream(rows.single())!!.use { it.readBytes() })
        assertTrue(f.journals.readAll().isEmpty())
    }

    @Test fun newRecoverySkipsActiveOldSaveAndDoesNotReplayItsCompletedJournal() = fixture { f ->
        val lease = SaveTransactionRegistry.tryAcquire(f.journals.transactionKey(f.id.value, 1))!!
        lease.use {
            f.journals.write(f.record)
            f.recovery().recover()
            assertTrue(f.rows().isEmpty())
            assertTrue(f.source.exists())
            val uri = CaptureSaver.publish(f.resolver, f.source, f.originalName, f.time, motionPhoto = false)
            f.published += uri
            f.journals.write(f.record.copy(originalUri = uri.toString(), completedStages = setOf(SaveStage.COMPLETE.name)))
        }
        f.recovery().recover()
        assertEquals(f.published, f.rows())
        assertTrue(f.journals.readAll().isEmpty())
    }

    private fun fixture(test: (Fixture) -> Unit) {
        val f = Fixture()
        try { test(f) } finally {
            (f.published + f.rows()).distinct().forEach { CaptureSaver.deleteQuietly(f.resolver, it) }
            f.directory.deleteRecursively()
        }
    }

    private class Fixture {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val id = CaptureId(UUID.randomUUID().toString().replace("-", ""))
        val time = System.currentTimeMillis()
        val directory = File(context.cacheDir, "recovery-test-${id.value}").apply { mkdirs() }
        val source = File(directory, "original.jpg").also { file ->
            val bitmap = Bitmap.createBitmap(8, 6, Bitmap.Config.ARGB_8888)
            try { file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) } }
            finally { bitmap.recycle() }
        }
        val originalBytes = source.readBytes()
        val packaged = File(directory, "broken.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val recipes = File(directory, "recipes")
        val journals = SaveJournalStore(File(directory, "journal"))
        val originalName = CaptureIdentity.displayName(id, CaptureAssetKind.ORIGINAL, 1, time)
        val record = SaveJournal(captureId = id.value, sequence = 1, takenAtMillis = time,
            sourcePath = source.absolutePath, packagedPath = packaged.absolutePath,
            displayName = CaptureIdentity.motionPhotoDisplayName(id, 1, time), style = CreativeStyle.ORIGINAL.name,
            motionPhotoRequested = true, completedStages = setOf(SaveStage.SPACE_CHECK.name, SaveStage.MOTION_PACKAGE.name))
        val published = mutableListOf<Uri>()
        val collection: Uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        fun recovery() = InterruptedSaveRecovery(resolver, File(directory, "motion"), journals,
            EditRecipeStore(recipes), CreativeImageProcessor(directory))
        fun rows(): List<Uri> = resolver.query(collection, arrayOf(MediaStore.Images.Media._ID),
            "${MediaStore.Images.Media.DISPLAY_NAME} IN (?, ?)", arrayOf(originalName, record.displayName), null)!!.use { cursor ->
            buildList { while (cursor.moveToNext()) add(Uri.withAppendedPath(collection, cursor.getLong(0).toString())) }
        }
    }
}
