package com.photocoach.app.camera

import android.graphics.Bitmap
import android.net.Uri
import android.os.Process
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.creative.*
import com.photocoach.app.research.*
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Execute the phases in separate instrumentation processes with am force-stop between them. */
class ProcessRecoveryAcceptanceTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private val root=File(context.noBackupFilesDir,"acceptance-process-recovery")
    private val journals get()=SaveJournalStore(File(root,"journal"))
    private val receipts get()=ResearchRecoveryStore(File(root,"receipts"))
    @Test fun prepareCommittedButUnjournaledOriginal() {
        check(!root.exists()) {"previous owned process fixture requires recovery first"}
        check(root.mkdirs())
        File(root,"pid").writeText(Process.myPid().toString())
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val models=androidx.lifecycle.ViewModelStore()
        lateinit var vm:com.photocoach.app.AppViewModel
        lateinit var spec:CaptureSpec
        val eventFile=File(root,"events.jsonl")
        instrumentation.runOnMainSync {
            vm=com.photocoach.app.AppViewModel(context.applicationContext as android.app.Application,
                com.photocoach.coach.ResearchProtocol(com.photocoach.coach.ResearchCondition.DYNAMIC,
                    com.photocoach.coach.ResearchScene.WINDOW,"synthetic-process-recovery"),ResearchEventLogger(eventFile))
            models.put("vm",vm);vm.onCameraReady(CameraCapabilities());vm.onResearchParametersApplied(vm.ui.value.sceneApply?.generation)
            assertTrue(vm.beginCapture());spec=vm.activeCaptureSpec()!!;vm.recordCaptureAccepted(spec)
        }
        val id=spec.captureId;val now=spec.takenAtMillis
        val source=File(root,"source.jpg")
        Bitmap.createBitmap(16,12,Bitmap.Config.ARGB_8888).let {bitmap ->
            try {source.outputStream().use {assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG,90,it))}} finally {bitmap.recycle()}}
        val record=SaveJournal(captureId=id.value,sequence=1,takenAtMillis=now,sourcePath=source.absolutePath,
            displayName=CaptureIdentity.displayName(id,CaptureAssetKind.ORIGINAL,1,now),style=CreativeStyle.ORIGINAL.name,
            researchContext=requireNotNull(spec.researchContext))
        journals.write(record)
        val failure=runCatching {CaptureSaver.publish(context.contentResolver,source,record.displayName,now,false,
            onPendingCreated={uri -> journals.write(record.copy(pendingUri=uri.toString()));File(root,"uri").writeText(uri.toString())},
            onAssetStage={if(it==AssetPublishStage.MEDIASTORE_COMMIT)error("synthetic interruption after commit")})}
        assertTrue(failure.isFailure)
        instrumentation.runOnMainSync {vm.onSaveFailed(failure.exceptionOrNull()!!,true,id.value)}
        val deadline=android.os.SystemClock.elapsedRealtime()+5000
        while((!eventFile.exists() || !eventFile.readText().contains("failed_retryable")) && android.os.SystemClock.elapsedRealtime()<deadline)
            android.os.SystemClock.sleep(20)
        assertTrue(eventFile.readText().contains("failed_retryable"))
        instrumentation.runOnMainSync {models.clear()}
        assertNull(journals.readAll().single().originalUri)
        assertTrue(source.isFile)
    }
    @Test fun recoverInNewProcessWithoutRepublishing() {
        check(root.isDirectory)
        val firstPid=File(root,"pid").readText().toInt()
        assertNotEquals("must be a genuinely new process",firstPid,Process.myPid())
        val original=journals.readAll().single()
        val uri=Uri.parse(File(root,"uri").readText())
        val bytes=File(original.sourcePath).readBytes()
        try {
            val recovery=InterruptedSaveRecovery(context.contentResolver,File(root,"motion"),journals,
                EditRecipeStore(File(root,"recipes")),CreativeImageProcessor(File(root,"processing")),researchRecoveryStore=receipts)
            assertTrue(recovery.recover().isEmpty())
            assertArrayEquals(bytes,context.contentResolver.openInputStream(uri)!!.use {it.readBytes()})
            val receipt=receipts.readAll().single()
            assertEquals(original.captureId,receipt.captureId);assertEquals(original.researchContext,receipt.context)
            assertNull(receipt.originalSessionPublicationElapsedMs)
            val bundle=ResearchEvidenceBundles.export(File(root,"events.jsonl"),receipts)
            val imported=ResearchEvidenceBundles.read(bundle)
            assertFalse("abrupt session has no closure",imported.log.complete)
            assertTrue(imported.log.issues.toString(),imported.log.issues.any {it.contains("closure") || it.contains("session_exit")})
            assertEquals(setOf(original.captureId),ResearchRecoveryAssociation.join(imported.log.events,imported.receipts).recoveredCaptureIds)
            val replay=imported.replay(setOf(ResearchRoundKey(receipt.context.sessionId,receipt.context.roundId)),30000).single()
            assertEquals(ResearchRoundOutcome.INCOMPLETE,replay.outcome);assertNull(replay.captureLatencyMs)
            val policy=CalculationPolicy(false,MissingChoiceRule.EXCLUDE,TieChoiceRule.EXCLUDE,FailedTimeRule.EXCLUDE_PAIR,
                IncrementBaseline.DIRECT_DYNAMIC_STATIC_ABOVE_HALF,30000,6,8,true,5000)
            val calculation=ResearchMetricCalculation.calculate(listOf(TrialMeasurement(RegisteredTrial(replay.key,"synthetic",
                receipt.context.condition,receipt.context.scene,1),replay,emptySet())),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),policy)
            assertNull(calculation.metrics);assertTrue(calculation.blockers.any {it.contains("incomplete")})
            assertFalse(File(original.sourcePath).exists())
            assertTrue(recovery.recover().isEmpty())
            val collection=android.provider.MediaStore.Images.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
            context.contentResolver.query(collection,arrayOf("_id"),"_display_name = ?",arrayOf(original.displayName),null)!!.use {assertEquals(1,it.count)}
            val evidence=File(context.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
            File(evidence,"actual-process-recovery-bundle.json").writeText(bundle)
            File(evidence,"actual-process-recovery.txt").writeText("firstPid=$firstPid\nrecoveryPid=${Process.myPid()}\noriginalIdentity=${receipt.context}\ncaptureId=${receipt.captureId}\npublicationElapsedMs=Unknown\npublishedRows=1\nsourceCleaned=true\n")
        } finally {context.contentResolver.delete(uri,null,null);root.deleteRecursively()}
    }
}
