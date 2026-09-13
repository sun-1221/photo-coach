package com.photocoach.app.camera

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.creative.CaptureIdentity
import com.photocoach.app.creative.CreativeStyle
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.photocoach.app.ui.viewfinder.ViewfinderScreen
import com.photocoach.app.ui.viewfinder.ViewfinderActions
import com.photocoach.app.ui.theme.PhotoCoachTheme
import com.photocoach.app.ViewfinderUi
import com.photocoach.coach.GuidanceSnapshot
import com.photocoach.coach.GuidanceStage
import com.photocoach.coach.ShotIntent

class CameraBinderPreparationRetryTest {
    @get:Rule val compose=createComposeRule()
    @Test fun preparationFailureUsesSaveErrorAndRealBinderRetryPublishesSameCaptureOnce() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        val id=CaptureIdentity.create()
        val directory=File(context.noBackupFilesDir,"pending-captures").apply {mkdirs()}
        val source=File.createTempFile("retry-${id.value}-",".jpg",directory)
        val raw=File(directory,"${source.name}.capture-raw")
        val bitmap=Bitmap.createBitmap(20,16,Bitmap.Config.ARGB_8888)
        try {raw.outputStream().use {assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG,100,it))}} finally {bitmap.recycle()}
        val errors=mutableListOf<Throwable>();val result=AtomicReference<CapturedPhoto>()
        val saved=CountDownLatch(1);val binder=CameraBinder(context)
        val spec=CaptureSpec(id,1,System.currentTimeMillis(),CreativeStyle.ORIGINAL,
            saveStrategy=SaveStrategy.ORIGINAL_WITH_RECIPE,derivativeQuality=DerivativeQuality.FULL,livePhotoRequested=true)
        val record=PendingCapture(source,spec,{result.set(it);saved.countDown()},{errors.add(it)},{},0,0,
            jpegPreparation=JpegPreparation(raw.path,20,16,2,2,18,11,90,95),jpegRawPath=raw.path)
        record.coordinator.complete(SaveStage.SPACE_CHECK)
        try {
            instrumentation.runOnMainSync {binder.retainJpegPreparationFailure(record,java.io.IOException("injected preparation failure"))}
            assertEquals(1,errors.size);assertEquals(id.value,binder.pendingCaptureId)
            assertTrue(raw.isFile);assertEquals(SaveStage.ORIGINAL_PUBLISH,record.coordinator.snapshot.failedStage)
            compose.mainClock.autoAdvance=false
            compose.setContent {PhotoCoachTheme {
                ViewfinderScreen(ViewfinderUi(guidance=GuidanceSnapshot(ShotIntent.CLOSE_UP,false,
                    GuidanceStage.SaveFailed("保存失败，请重试",true),false,false,1)),0f,
                    ViewfinderActions(onPreviewReady={},onTapFocus={_,_,_->},onEv={},onSetFocal={},onZoomBy={},
                        onVoiceEnabledChange={},onSubjectCaptionsEnabledChange={},onGridEnabledChange={},onLevelEnabledChange={},
                        onTimerChange={},onAspectRatioChange={},onCapturePriorityChange={},onModePreferenceChange={},
                        onResetSettings={},onUnlockFocus={},onToggleFlash={},onSelectIntent={},onSkip={},onOptional={},
                        onCapture={},onOpenRecentPhoto={},onRetrySave={
                            assertTrue(binder.retrySave(id.value,{result.set(it);saved.countDown()},{errors.add(it);saved.countDown()}))
                        },onDiscardSave={},onRetryCamera={},onOpenSettings={},onExit={},onHideFocusControls={},onDismissControlMessage={}))
            }}
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithTag("save_retry").performClick()
            assertTrue(saved.await(15,TimeUnit.SECONDS));assertEquals(1,errors.size)
            val photo=requireNotNull(result.get());assertEquals(id,photo.captureId)
            context.contentResolver.openInputStream(photo.originalUri)!!.use {input ->
                val decoded=android.graphics.BitmapFactory.decodeStream(input)!!
                try {assertEquals(16,decoded.width);assertEquals(9,decoded.height)} finally {decoded.recycle()}
            }
            instrumentation.runOnMainSync {assertFalse(binder.retrySave(id.value,{},{}))}
            assertFalse(raw.exists());assertFalse(source.exists())
        } finally {
            instrumentation.runOnMainSync {binder.release()}
            result.get()?.let {context.contentResolver.delete(it.originalUri,null,null)}
            raw.delete();source.delete()
            SaveJournalStore(File(context.filesDir,"save-journal")).delete(record.toJournal())
        }
    }
}
