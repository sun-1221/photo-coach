package com.photocoach.app

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.camera.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OriginalPublicationFeedbackTest {
    @Test fun retryCallbackForAAfterBStartsCannotOverwriteBAndRefreshesARecovery() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val store = ViewModelStore()
            val app = instrumentation.targetContext.applicationContext as Application
            val directory = java.io.File(app.noBackupFilesDir, "retry-callback-test-${java.util.UUID.randomUUID()}").apply { mkdirs() }
            val published = mutableListOf<android.net.Uri>()
            try {
                val vm = AppViewModel(app, com.photocoach.coach.ResearchProtocol(com.photocoach.coach.ResearchCondition.NONE,
                    com.photocoach.coach.ResearchScene.WINDOW, ""))
                store.put("vm", vm)
                vm.setThreeShotBurstEnabled(false)
                vm.onCameraReady(CameraCapabilities())
                vm.setSaveStrategy(SaveStrategy.ORIGINAL_AND_EFFECT)
                vm.setCreativeStyle(com.photocoach.app.creative.CreativeStyle.NATURAL_PORTRAIT)
                assertTrue(vm.beginCapture()); val a = vm.activeCaptureSpec()!!
                val source = java.io.File(directory, "A.jpg")
                val bitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
                try { source.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) } }
                finally { bitmap.recycle() }
                val journals = SaveJournalStore(java.io.File(directory, "journal"))
                var record = PendingCapture(source, a, {}, {}, vm::onSaveProgress, 0, 0).toJournal()
                    .copy(failedStage = SaveStage.ORIGINAL_PUBLISH.name, error = "first original attempt failed")
                assertTrue(record.derivativeRequested)
                journals.write(record)
                var refreshes = 0
                fun failures(id: String?) = CaptureSaveFailureHandler(id, { true }, vm::onSaveFailed,
                    { refreshes++; vm.onInterruptedSaves(journals.readAll()) }, {})
                failures(a.captureId.value).saveError(java.io.IOException("first original attempt failed"))
                assertTrue(vm.ui.value.guidance.stage is com.photocoach.coach.GuidanceStage.SaveFailed)
                val originals = ArrayDeque<Runnable>(); val effects = ArrayDeque<Runnable>()
                val pipeline = OriginalFirstSavePipeline(java.util.concurrent.Executor { originals.add(it) },
                    java.util.concurrent.Executor { effects.add(it) }, java.util.concurrent.Executor { it.run() })
                val originalSaved = SaveSnapshot(SavePlan(false, true), setOf(SaveStage.SPACE_CHECK, SaveStage.ORIGINAL_PUBLISH))
                var uriA: android.net.Uri? = null
                // This is MainActivity's production retry wiring, not a manually identity-corrected VM failure call.
                retryCapturedSave<Unit>(captureId = record.captureId, begin = vm::beginRetrySave,
                    retry = { requestedId, saved, error ->
                        assertEquals(record.captureId, requestedId)
                        pipeline.run(publishOriginal = {
                            uriA = CaptureSaver.publish(app.contentResolver, source, "RETRY_A_${a.captureId.value}.jpg")
                            published += uriA!!
                            record = record.copy(originalUri = uriA.toString(), failedStage = null, error = null,
                                completedStages = originalSaved.completed.map(SaveStage::name).toSet())
                            journals.write(record)
                        }, originalReady = {
                            vm.onSaveProgress(CaptureSaveProgress(a.captureId.value, originalSaved, uriA.toString(), true))
                        }, finish = {
                            record = record.copy(failedStage = SaveStage.DERIVATIVE_GENERATE.name, error = "A effect failed",
                                completedStages = record.completedStages + SaveStage.RECIPE_WRITE.name)
                            journals.write(record)
                            throw PartialSaveException("A effect failed", uriA!!, java.io.IOException("injected"))
                        }, completed = { it.fold(onSuccess = saved, onFailure = error) })
                        true
                    }, saved = {}, failureFor = ::failures)
                originals.removeFirst().run()
                assertTrue(vm.beginCapture()); val b = vm.activeCaptureSpec()!!
                val uriB = CaptureSaver.publish(app.contentResolver, source, "RETRY_B_${b.captureId.value}.jpg")
                published += uriB
                vm.onSaveProgress(CaptureSaveProgress(b.captureId.value, originalSaved, uriB.toString()))
                val before = vm.ui.value.guidance.stage
                effects.removeFirst().run() // A fails through the callback supplied by retryCapturedSave.
                assertEquals(before, vm.ui.value.guidance.stage)
                assertEquals(b.captureId, vm.activeCaptureSpec()!!.captureId)
                assertEquals(uriB.toString(), vm.ui.value.recentPhoto)
                assertEquals(2, refreshes)
                assertTrue(source.isFile)
                assertEquals("A effect failed", journals.readAll().single().error)
                assertEquals(a.captureId.value, vm.ui.value.recoveryRecords.single().captureId)
            } finally {
                published.forEach { app.contentResolver.delete(it, null, null) }
                store.clear()
                directory.deleteRecursively()
            }
        }
    }
    @Test fun originalPipelineReleasesNewCaptureWhileOldEffectIsSuspendedAndLateFailureCannotLockIt() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val store = ViewModelStore()
            var originalUri: android.net.Uri? = null
            var originalSource: java.io.File? = null
            try {
                val app = instrumentation.targetContext.applicationContext as Application
                val vm = AppViewModel(app, com.photocoach.coach.ResearchProtocol(com.photocoach.coach.ResearchCondition.NONE,
                    com.photocoach.coach.ResearchScene.WINDOW, ""))
                store.put("vm", vm)
                vm.setThreeShotBurstEnabled(false)
                vm.onCameraReady(CameraCapabilities())
                val originals = ArrayDeque<Runnable>(); val effects = ArrayDeque<Runnable>()
                val pipeline = OriginalFirstSavePipeline(java.util.concurrent.Executor { originals.add(it) },
                    java.util.concurrent.Executor { effects.add(it) }, java.util.concurrent.Executor { it.run() })
                assertTrue(vm.beginCapture()); val first = vm.activeCaptureSpec()!!
                val source = java.io.File.createTempFile("original-first-test-", ".jpg", app.noBackupFilesDir)
                originalSource = source
                val bitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
                try { source.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) } }
                finally { bitmap.recycle() }
                val snapshot = SaveSnapshot(SavePlan(false, true), setOf(SaveStage.SPACE_CHECK, SaveStage.ORIGINAL_PUBLISH))
                pipeline.run(publishOriginal = {
                    originalUri = CaptureSaver.publish(app.contentResolver, source, "PIPELINE_${first.captureId.value}.jpg")
                    vm.onSaveProgress(CaptureSaveProgress(first.captureId.value, snapshot, originalUri.toString()))
                },
                    originalReady = { vm.onSaveProgress(CaptureSaveProgress(first.captureId.value, snapshot, originalUri.toString(), true)) },
                    finish = { error("suspended old effect eventually fails") },
                    completed = { vm.onSaveFailed(it.exceptionOrNull()!!, true, first.captureId.value) })
                originals.removeFirst().run()
                assertTrue(vm.ui.value.guidance.shutterEnabled)
                assertTrue(vm.beginCapture()); val second = vm.activeCaptureSpec()!!
                assertNotEquals(first.captureId, second.captureId)
                val secondStage = vm.ui.value.guidance.stage
                effects.removeFirst().run()
                assertEquals(secondStage, vm.ui.value.guidance.stage)
                assertEquals(second.captureId, vm.activeCaptureSpec()!!.captureId)
            } finally {
                originalUri?.let { instrumentation.targetContext.contentResolver.delete(it, null, null) }
                originalSource?.delete()
                store.clear()
            }
        }
    }

    @Test fun researchFastNextShutterPreparesBeforeCaptureAndSlowConfirmationRestartsCueClock() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val store = ViewModelStore()
            try {
                var clock = 0L
                val app = instrumentation.targetContext.applicationContext as Application
                val protocol = com.photocoach.coach.ResearchProtocol(com.photocoach.coach.ResearchCondition.STATIC,
                    com.photocoach.coach.ResearchScene.WINDOW, "instrumented")
                val vm = AppViewModel(app, protocol) { clock }
                store.put("vm", vm)
                vm.onCameraReady(CameraCapabilities())
                clock = 10_000
                vm.onResearchParametersApplied(vm.ui.value.sceneApply?.generation)
                assertEquals(clock, (vm.ui.value.guidance.stage as com.photocoach.coach.GuidanceStage.Action).shownAtMs)
                assertTrue(vm.beginCapture()); val first = vm.activeCaptureSpec()!!
                vm.onSaveProgress(CaptureSaveProgress(first.captureId.value,
                    SaveSnapshot(SavePlan(false, false), setOf(SaveStage.SPACE_CHECK, SaveStage.ORIGINAL_PUBLISH)), "original", true))
                clock += 1
                assertFalse(vm.beginCapture()) // common entry for screen and volume shutter
                assertTrue(vm.ui.value.researchRoundPreparing)
                assertFalse(vm.beginCapture())
                val generation = vm.ui.value.sceneApply!!.generation
                vm.onResearchParametersApplied(generation - 1)
                assertTrue(vm.ui.value.researchRoundPreparing)
                clock += 10_000
                vm.onResearchParametersApplied(generation)
                assertEquals(clock, (vm.ui.value.guidance.stage as com.photocoach.coach.GuidanceStage.Action).shownAtMs)
                assertTrue(vm.beginCapture())
            } finally { store.clear() }
        }
    }
    @Test fun originalPublicationImmediatelyUpdatesThumbnailAndDoesNotBecomeTotalFailure() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val store = ViewModelStore()
            try {
                val app = instrumentation.targetContext.applicationContext as Application
                val vm = ViewModelProvider(store, ViewModelProvider.AndroidViewModelFactory(app))[AppViewModel::class.java]
                vm.onCameraReady(CameraCapabilities())
                // Research builds require the reset rebind acknowledgement before the shutter.
                if (vm.ui.value.researchRoundPreparing) {
                    vm.onCameraReady(CameraCapabilities(analysisSessionId = 1))
                    vm.onResearchParametersApplied(vm.ui.value.sceneApply?.generation)
                }
                assertTrue(vm.beginCapture())
                val spec = vm.activeCaptureSpec()!!
                val uri = "content://media/external/images/media/123"
                val savedOriginal = SaveSnapshot(SavePlan(false, true),
                    completed = setOf(SaveStage.SPACE_CHECK, SaveStage.ORIGINAL_PUBLISH))
                vm.onSaveProgress(CaptureSaveProgress(spec.captureId.value, savedOriginal, uri))
                assertEquals(uri, vm.ui.value.recentPhoto)
                assertTrue(vm.ui.value.saveStatusText!!.startsWith("原片已保存到系统相册"))
                vm.onSaveProgress(CaptureSaveProgress(spec.captureId.value,
                    savedOriginal.copy(failedStage = SaveStage.RECIPE_WRITE, error = "injected"), uri))
                assertTrue(vm.ui.value.savePartialSuccess)
                assertTrue(vm.ui.value.saveStatusText!!.startsWith("原片已保存"))
                assertEquals(uri, vm.ui.value.recentPhoto)
            } finally { store.clear() }
        }
    }
}
