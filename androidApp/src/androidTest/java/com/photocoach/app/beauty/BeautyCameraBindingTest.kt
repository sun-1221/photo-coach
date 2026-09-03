package com.photocoach.app.beauty

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.os.SystemClock
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class BeautyCameraBindingTest {
    @Test fun threeUseCasesCaptureAndReleaseAcrossEffectRebinds() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=ApplicationProvider.getApplicationContext<Context>()
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.CAMERA)
        }
        assertEquals("camera permission is required for the binding test", PackageManager.PERMISSION_GRANTED,
            context.checkSelfPermission(Manifest.permission.CAMERA))
        val provider=ProcessCameraProvider.getInstance(context).get(10,TimeUnit.SECONDS)
        val main=ContextCompat.getMainExecutor(context)
        val worker=Executors.newSingleThreadExecutor()
        val error=AtomicReference<Throwable?>()
        try {
            repeat(2) {
                val previewFrame=CountDownLatch(1)
                val analyzed=CountDownLatch(1)
                val released=CountDownLatch(1)
                val captured=CountDownLatch(1)
                val owner=object : LifecycleOwner {
                    val registry=LifecycleRegistry(this)
                    override val lifecycle: Lifecycle get() = registry
                }
                val effect=BeautyCameraEffect(BeautyFaceStore(),{ BeautyPreset.NATURAL },{ true }) { failure ->
                    error.compareAndSet(null,failure)
                }
                val preview=Preview.Builder().build()
                val analysis=ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                val capture=ImageCapture.Builder().build()
                analysis.setAnalyzer(worker) { image -> image.close(); analyzed.countDown() }
                try {
                    instrumentation.runOnMainSync {
                        owner.registry.currentState=Lifecycle.State.RESUMED
                        preview.setSurfaceProvider(main) { request ->
                            val texture=SurfaceTexture(false)
                            texture.setDefaultBufferSize(request.resolution.width,request.resolution.height)
                            texture.setOnFrameAvailableListener { previewFrame.countDown() }
                            val surface=Surface(texture)
                            request.provideSurface(surface,main) {
                                texture.setOnFrameAvailableListener(null)
                                surface.release(); texture.release(); released.countDown()
                            }
                        }
                        provider.bindToLifecycle(owner,CameraSelector.DEFAULT_BACK_CAMERA,
                            UseCaseGroup.Builder().addUseCase(preview).addUseCase(analysis)
                                .addUseCase(capture).addEffect(effect).build())
                    }
                    assertTrue("analysis remains active",analyzed.await(10,TimeUnit.SECONDS))
                    assertTrue("processed preview reaches output",previewFrame.await(10,TimeUnit.SECONDS))
                    instrumentation.runOnMainSync {
                        capture.takePicture(worker,object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                try {
                                    if(image.format != ImageFormat.JPEG || image.width<=0 || image.height<=0)
                                        error.compareAndSet(null,AssertionError("capture must remain a nonempty JPEG"))
                                } finally { image.close(); captured.countDown() }
                            }
                            override fun onError(exception: ImageCaptureException) {
                                error.compareAndSet(null,exception); captured.countDown()
                            }
                        })
                    }
                    assertTrue("ImageCapture remains available",captured.await(10,TimeUnit.SECONDS))
                    assertNull(error.get())
                } finally {
                    instrumentation.runOnMainSync {
                        provider.unbindAll(); effect.close(); owner.registry.currentState=Lifecycle.State.DESTROYED
                    }
                    assertTrue("CameraX output ownership released",released.await(10,TimeUnit.SECONDS))
                }
            }
            val deadline=SystemClock.elapsedRealtime()+5_000
            while(Thread.getAllStackTraces().keys.any { it.name=="beauty-gl" && it.isAlive } &&
                SystemClock.elapsedRealtime()<deadline) SystemClock.sleep(50)
            assertFalse("no effect worker retained after unbind",
                Thread.getAllStackTraces().keys.any { it.name=="beauty-gl" && it.isAlive })
        } finally { worker.shutdownNow() }
    }
}
