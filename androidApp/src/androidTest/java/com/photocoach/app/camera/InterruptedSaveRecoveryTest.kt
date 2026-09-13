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
    @Test fun discardCleanupFailureRetainsTombstoneAndRecoveryNeverPublishesIt()=fixture {f ->
        val raw=File(f.directory,"retained-raw.jpg");f.source.copyTo(raw)
        val record=f.record.copy(jpegRawPath=raw.path,motionPhotoRequested=false,packagedPath=null,displayName=f.originalName)
        assertTrue(runCatching {discardCaptureSources(f.resolver,f.journals,record){file ->if(file==raw)false else file.delete()}}.isFailure)
        assertTrue(raw.isFile);assertTrue(f.journals.readAll().single().discarded)
        assertNotNull(f.journals.readAll().single().error)
        assertTrue(f.recovery().recover().isEmpty())
        assertFalse(raw.exists());assertTrue(f.rows().isEmpty());assertTrue(f.journals.readAll().isEmpty())
    }
    @Test fun fullQuotaDefersJpegPreparationWithoutTouchingRawOrPublishing()=fixture {f ->
        val raw=File(f.directory,"raw.jpg");f.source.copyTo(raw)
        val preparation=JpegPreparation(raw.path,8,6,1,1,7,5,90,95)
        f.journals.write(f.record.copy(motionPhotoRequested=false,packagedPath=null,displayName=f.originalName,
            jpegPreparation=preparation,jpegRawPath=raw.path))
        val storage=PendingSourceStore(f.directory,PendingStorageLimits(quotaBytes=500,livePeakBytes=1000))
        assertTrue(f.recovery(storage).recover().single().error!!.contains("配额"))
        assertArrayEquals(f.originalBytes,raw.readBytes());assertArrayEquals(f.originalBytes,f.source.readBytes())
        assertEquals(preparation,f.journals.readAll().single().jpegPreparation);assertTrue(f.rows().isEmpty())
    }
    @Test fun interruptedJpegPreparationReplaysRawOnceBeforePublishingSameCapture() = fixture {f ->
        val raw=File(f.directory,"raw.jpg");f.source.copyTo(raw)
        val preparation=JpegPreparation(raw.path,8,6,1,1,7,5,90,95)
        preparation.prepare(f.source) // Crash after prepared-file move, before clearing its journal.
        f.journals.write(f.record.copy(motionPhotoRequested=false,packagedPath=null,displayName=f.originalName,
            jpegPreparation=preparation))
        assertTrue(f.recovery().recover().isEmpty())
        val uri=f.rows().single();f.published+=uri
        f.resolver.openInputStream(uri)!!.use {input ->
            val bitmap=android.graphics.BitmapFactory.decodeStream(input)!!
            try {assertEquals(6,bitmap.width);assertEquals(4,bitmap.height)} finally {bitmap.recycle()}
        }
        assertFalse(raw.exists());assertFalse(f.source.exists());assertTrue(f.journals.readAll().isEmpty())
        assertTrue(f.recovery().recover().isEmpty());assertEquals(listOf(uri),f.rows())
    }
    @Test fun completePendingJpegAndMotionAreCommittedBeforeSourceCleanup() {
        for(motion in listOf(false,true))fixture {f ->
            if(motion) {
                val video=File(f.directory,"valid.mp4");PlayableMotionPhotoTest().encode(video)
                assertTrue(f.packaged.delete()) // This fixture initially contains deliberately corrupt bytes.
                MotionPhotoAssembler.assemble(f.source,video,f.packaged,1_000_000)
            }
            val source=if(motion)f.packaged else f.source
            val bytes=source.readBytes()
            val name=if(motion)f.record.displayName else f.originalName
            val uri=f.resolver.insert(f.collection,ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME,name);put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH,CaptureSaver.RELATIVE_DIR);put(MediaStore.Images.Media.IS_PENDING,1)
            })!!
            f.published+=uri
            f.resolver.openOutputStream(uri,"w")!!.use {it.write(bytes)}
            PublishedAssetVerifier.verifyPending(f.resolver,uri,motion)
            assertTrue(runCatching {PublishedAssetVerifier.verifyPublished(f.resolver,uri,name,CaptureSaver.RELATIVE_DIR,motion)}.isFailure)
            f.journals.write(f.record.copy(motionPhotoRequested=motion,packagedPath=if(motion)f.packaged.path else null,
                displayName=name,pendingUri=uri.toString(),verifiedAssetStages=setOf(assetStageKey(PublishedAssetKind.ORIGINAL,AssetPublishStage.VERIFY_PENDING))))
            assertTrue(f.source.isFile)
            assertTrue(f.recovery().recover().isEmpty())
            assertEquals(listOf(uri),f.rows())
            f.resolver.query(uri,arrayOf(MediaStore.Images.Media.IS_PENDING),null,null,null)!!.use {assertTrue(it.moveToFirst());assertEquals(0,it.getInt(0))}
            PublishedAssetVerifier.verifyPublished(f.resolver,uri,name,CaptureSaver.RELATIVE_DIR,motion)
            assertArrayEquals(bytes,f.resolver.openInputStream(uri)!!.use {it.readBytes()})
            assertFalse(f.source.exists());assertTrue(f.journals.readAll().isEmpty())
        }
    }
    @Test fun committedMotionWithLostPackageKeepsOriginalUriAndContainer() = fixture {f ->
        val video=File(f.directory,"valid.mp4");PlayableMotionPhotoTest().encode(video)
        assertTrue(f.packaged.delete()) // A successful assembly requires a fresh output.
        MotionPhotoAssembler.assemble(f.source,video,f.packaged,1_000_000)
        val bytes=f.packaged.readBytes()
        var uri:Uri?=null
        assertTrue(runCatching {CaptureSaver.publish(f.resolver,f.packaged,f.record.displayName,f.time,true,
            onAssetStage={if(it==AssetPublishStage.MEDIASTORE_COMMIT)error("interrupted before journal public state")},
            onPendingCreated={uri=it;f.published+=it;f.journals.write(f.record.copy(pendingUri=it.toString()))})}.isFailure)
        assertTrue(f.packaged.delete());assertTrue(f.source.exists())
        assertTrue(f.recovery().recover().isEmpty())
        assertEquals(listOf(uri),f.rows())
        PublishedAssetVerifier.verifyPublished(f.resolver,uri!!,f.record.displayName,CaptureSaver.RELATIVE_DIR,true)
        assertArrayEquals(bytes,f.resolver.openInputStream(uri!!)!!.use {it.readBytes()})
        assertFalse(f.source.exists());assertTrue(f.journals.readAll().isEmpty())
    }
    @Test fun reloadedResearchJournalPublishesReceiptWithOriginalIdentityAndUnknownLatency() = fixture { f ->
        val owner=com.photocoach.app.research.ResearchCaptureContext("original-session",7,"close_up","DYNAMIC","WINDOW","synthetic","test")
        f.journals.write(f.record.copy(motionPhotoRequested=false,packagedPath=null,displayName=f.originalName,
            researchContext=owner,completedStages=setOf(SaveStage.SPACE_CHECK.name)))
        val receiptDirectory=File(f.context.noBackupFilesDir,"recovery-test-${f.id.value}")
        try {
            fun fresh()=InterruptedSaveRecovery(f.resolver,File(f.directory,"motion"),SaveJournalStore(File(f.directory,"journal")),
                EditRecipeStore(f.recipes),CreativeImageProcessor(f.directory),
                researchRecoveryStore=com.photocoach.app.research.ResearchRecoveryStore(receiptDirectory))
            assertTrue(fresh().recover().isEmpty())
            val rows=f.rows();f.published+=rows;assertEquals(1,rows.size)
            assertArrayEquals(f.originalBytes,f.resolver.openInputStream(rows.single())!!.use {it.readBytes()})
            val receipt=com.photocoach.app.research.ResearchRecoveryStore(receiptDirectory).readAll().single()
            assertEquals(owner,receipt.context);assertEquals(f.id.value,receipt.captureId)
            assertNull(receipt.originalSessionPublicationElapsedMs)
            assertTrue(fresh().recover().isEmpty());assertEquals(rows,f.rows())
            val evidence=File(f.context.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
            receiptDirectory.listFiles()!!.single().copyTo(File(evidence,"synthetic-cross-instance-recovery.json"),overwrite=true)
        } finally {receiptDirectory.deleteRecursively()}
    }
    @Test fun fullQuotaRejectsNewAndRecoveryGenerationButKeepsExistingSourcePublishable() = fixture { f ->
        val original = CaptureSaver.publish(f.resolver, f.source, f.originalName, f.time, motionPhoto = false)
        f.published += original
        val durable = File(f.directory, "durable").apply { mkdirs() }
        val retained = File(durable, "failed-effect.jpg").apply { writeBytes(f.originalBytes) }
        val storage = PendingSourceStore(durable, PendingStorageLimits(
            quotaBytes = retained.length() + 500, stillPeakBytes = 100, creativePeakBytes = 1000,
            livePeakBytes = 1000, reserveBytes = 10))
        fun request() = CaptureIdentity.create().let { derivative ->
            f.record.copy(motionPhotoRequested = false, packagedPath = null, sourcePath = "", completedStages = emptySet(),
                derivativeId = derivative.value, exportSourceUri = original.toString(),
                displayName = CaptureIdentity.displayName(derivative, CaptureAssetKind.EDITED, 1, f.time))
        }
        val fresh = request()
        assertTrue(runCatching { f.recovery(storage).createExport(fresh) }.exceptionOrNull()!!.message!!.contains("配额"))
        assertTrue(runCatching { f.recovery(storage).recoverExport(fresh) }.exceptionOrNull()!!.message!!.contains("配额"))
        assertArrayEquals(f.originalBytes, retained.readBytes())
        assertNotNull(f.journals.read(fresh)?.error)
        val existing = request().copy(derivativePath = retained.absolutePath,
            completedStages = setOf(SaveStage.DERIVATIVE_GENERATE.name))
        f.journals.write(existing)
        val saved = f.recovery(storage).recoverExport(existing)
        f.published += saved.uri
        assertArrayEquals(f.originalBytes, f.resolver.openInputStream(saved.uri)!!.use { it.readBytes() })
    }
    @Test fun missingSourceWithPublishedPendingIdentityIsVerifiedWithoutDeletingOrRepublishing() {
        for (schema in 1..3) fixture { f ->
            val uri = CaptureSaver.publish(f.resolver, f.source, f.originalName, f.time, motionPhoto = false)
            f.published += uri
            f.source.delete()
            f.journals.write(f.record.copy(schemaVersion = schema, motionPhotoRequested = false, packagedPath = null,
                displayName = f.originalName, pendingUri = uri.toString(), completedStages = setOf(SaveStage.SPACE_CHECK.name)))
            f.recovery().recover()
            assertTrue(f.journals.readAll().isEmpty())
            assertEquals(listOf(uri), f.rows())
            assertArrayEquals(f.originalBytes, f.resolver.openInputStream(uri)!!.use { it.readBytes() })
        }
    }

    @Test fun commitThenJournalFailureRetainsPublishedRowAndRecoveryReusesIt() = fixture { f ->
        var uri: Uri? = null
        val request = f.record.copy(motionPhotoRequested = false, packagedPath = null,
            displayName = f.originalName, completedStages = setOf(SaveStage.SPACE_CHECK.name))
        val failure = runCatching {
            CaptureSaver.publish(f.resolver, f.source, f.originalName, f.time, motionPhoto = false,
                onPendingCreated = { uri = it; f.published += it; f.journals.write(request.copy(pendingUri = it.toString())) },
                onAssetStage = { if (it == AssetPublishStage.MEDIASTORE_COMMIT) error("injected journal write failure") })
        }
        assertTrue(failure.isFailure)
        assertEquals(listOf(uri), f.rows())
        f.source.delete()
        f.recovery().recover()
        assertTrue(f.journals.readAll().isEmpty())
        assertEquals(listOf(uri), f.rows())
    }

    @Test fun missingSourceWithoutPublishedIdentityRemainsExplicitUnrecoverableRecord() = fixture { f ->
        f.source.delete()
        f.journals.write(f.record.copy(motionPhotoRequested = false, packagedPath = null,
            displayName = f.originalName, completedStages = emptySet()))
        f.recovery().recover()
        assertTrue(f.journals.readAll().single().error!!.contains("源文件已丢失"))
        assertTrue(f.rows().isEmpty())
    }
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
            f.journals.write(f.record.copy(originalUri = uri.toString(), motionPhotoRequested = false,
                packagedPath = null, displayName = f.originalName, completedStages = setOf(SaveStage.COMPLETE.name)))
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
        fun recovery(storage: PendingSourceStore? = null) = InterruptedSaveRecovery(resolver, File(directory, "motion"), journals,
            EditRecipeStore(recipes), CreativeImageProcessor(storage?.let { File(it.directory, "processing") } ?: directory), storage)
        fun rows(): List<Uri> = resolver.query(collection, arrayOf(MediaStore.Images.Media._ID),
            "${MediaStore.Images.Media.DISPLAY_NAME} IN (?, ?)", arrayOf(originalName, record.displayName), null)!!.use { cursor ->
            buildList { while (cursor.moveToNext()) add(Uri.withAppendedPath(collection, cursor.getLong(0).toString())) }
        }
    }
}
