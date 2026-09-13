package com.photocoach.app

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.camera.*
import com.photocoach.app.research.*
import com.photocoach.coach.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ResearchLogPipelineTest {
    @Test fun productionRejectionBeforeCameraAcceptanceDoesNotInventSaveAttempt() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val file=File(app.noBackupFilesDir,"synthetic-rejection-${java.util.UUID.randomUUID()}.jsonl")
        val store=ViewModelStore()
        try {
            instrumentation.runOnMainSync {
                val vm=AppViewModel(app,ResearchProtocol(ResearchCondition.DYNAMIC,ResearchScene.WINDOW,"synthetic-rejection"),ResearchEventLogger(file))
                store.put("vm",vm);vm.onCameraReady(CameraCapabilities());vm.onResearchParametersApplied(vm.ui.value.sceneApply?.generation)
                assertTrue(vm.beginCapture());val spec=vm.activeCaptureSpec()!!
                assertNotNull(spec.researchContext)
                vm.onSaveFailed(java.io.IOException("synthetic rejection before submission"),false,spec.captureId.value)
                vm.recordExit()
            }
            val deadline=android.os.SystemClock.elapsedRealtime()+5000
            while((!file.exists() || !file.readText().contains("session_exit")) && android.os.SystemClock.elapsedRealtime()<deadline)Thread.sleep(20)
            val imported=ResearchLogReader.read(file.readText());assertTrue(imported.issues.toString(),imported.complete)
            assertEquals(1,imported.events.count {it.type=="capture_rejected"})
            assertFalse(imported.events.any {it.type=="capture_accepted" || it.type=="save"})
            val start=imported.events.single {it.type=="round_operable"}
            val round=ResearchRecomputation.replay(imported,setOf(ResearchRoundKey(start.sessionId,start.roundId)),30000).single()
            assertEquals(ResearchRoundOutcome.FAILED,round.outcome);assertEquals(0,round.acceptedCaptures)
            val evidence=File(app.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
            file.copyTo(File(evidence,"synthetic-vm-rejected.jsonl"),overwrite=true)
        } finally {instrumentation.runOnMainSync {store.clear()};file.delete()}
    }
    @Test fun productionNoneModeNeverCreatesOrAppendsResearchFile() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val file=File(app.noBackupFilesDir,"synthetic-none-${java.util.UUID.randomUUID()}.jsonl")
        val store=ViewModelStore();val logger=ResearchEventLogger(file,maximumBytes=1)
        lateinit var vm:AppViewModel
        try {
            instrumentation.runOnMainSync {
                vm=AppViewModel(app,ResearchProtocol(ResearchCondition.NONE,ResearchScene.WINDOW,""),logger)
                store.put("vm",vm);vm.onCameraReady(CameraCapabilities());vm.recordExit()
            }
            Thread.sleep(200)
            assertFalse(file.exists());assertEquals(0L,logger.failedWrites)
            file.writeText("preexisting")
            instrumentation.runOnMainSync {vm.recordExit();vm.onCameraReady(CameraCapabilities())}
            Thread.sleep(200)
            instrumentation.runOnMainSync {store.clear()}
            assertEquals("preexisting",file.readText())
        } finally {instrumentation.runOnMainSync {store.clear()};file.delete()}
    }
    @Test fun productionVmFailureRetryPublicationExportsReplayableJsonl() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val app=instrumentation.targetContext.applicationContext as Application
        val file=File(app.noBackupFilesDir,"synthetic-research-${java.util.UUID.randomUUID()}.jsonl")
        val store=ViewModelStore()
        var clock=1000L
        lateinit var vm: AppViewModel
        lateinit var capture: CaptureSpec
        try {
            instrumentation.runOnMainSync {
                vm=AppViewModel(app,ResearchProtocol(ResearchCondition.DYNAMIC,ResearchScene.WINDOW,"synthetic-contract-v1"),
                    ResearchEventLogger(file),{clock})
                store.put("vm",vm)
                vm.onCameraReady(CameraCapabilities())
                vm.onResearchParametersApplied(vm.ui.value.sceneApply?.generation)
                clock+=1000
                assertTrue(vm.beginCapture());capture=vm.activeCaptureSpec()!!
                vm.recordCaptureAccepted(capture)
                clock+=1000
                vm.onSaveFailed(java.io.IOException("synthetic save failure"),true,capture.captureId.value)
                clock+=1000
                retryCapturedSave<Unit>(capture.captureId.value,vm::beginRetrySave,{ id,_,_ ->
                    assertEquals(capture.captureId.value,id);true },{}, { id ->
                    CaptureSaveFailureHandler(id,{true},vm::onSaveFailed,{},{}) })
                clock+=1000
                vm.onSaveProgress(CaptureSaveProgress(capture.captureId.value,
                    SaveSnapshot(SavePlan(false,false),setOf(SaveStage.SPACE_CHECK,SaveStage.ORIGINAL_PUBLISH)),
                    "content://synthetic/original",true))
                vm.recordExit()
            }
            val deadline=android.os.SystemClock.elapsedRealtime()+5000
            while ((!file.exists() || !file.readText().contains("session_exit")) && android.os.SystemClock.elapsedRealtime()<deadline)
                Thread.sleep(20)
            val imported=ResearchLogReader.read(file.readText())
            assertTrue(imported.issues.toString(),imported.complete)
            val events=imported.events
            val start=events.single {it.type=="round_operable"}
            val failure=events.single {it.type=="save" && it.result=="failed_retryable"}
            val retry=events.single {it.type=="save_retry"}
            assertEquals(capture.captureId.value,failure.captureId);assertEquals(failure.captureId,retry.captureId)
            assertEquals(start.roundId,failure.roundId)
            val result=ResearchRecomputation.replay(imported,setOf(ResearchRoundKey(start.sessionId,start.roundId)),30000).single()
            assertEquals(ResearchRoundOutcome.FAILED,result.outcome)
            assertTrue(result.firstAttemptFailed);assertTrue(result.recovered)
            assertEquals(4000L,result.captureLatencyMs)
            val evidence=File(instrumentation.targetContext.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
            file.copyTo(File(evidence,"synthetic-vm-research.jsonl"),overwrite=true)
        } finally { instrumentation.runOnMainSync {store.clear()};file.delete() }
    }
}
