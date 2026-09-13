package com.photocoach.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineModelInstrumentedTest {
    @Test fun bundledFaceAndPoseInitializeAndProcessSyntheticInputWithoutInternetPermission() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(PackageManager.PERMISSION_DENIED,context.checkSelfPermission(Manifest.permission.INTERNET))
        val face=FaceDetection.getClient(FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).setMinFaceSize(.05f).enableTracking().build())
        val pose=PoseDetection.getClient(PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build())
        val bitmap=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888)
        try {
            val input=InputImage.fromBitmap(bitmap,0)
            assertTrue(Tasks.await(face.process(input),30,TimeUnit.SECONDS).isEmpty())
            assertTrue(Tasks.await(pose.process(input),30,TimeUnit.SECONDS).allPoseLandmarks.isEmpty())
        } finally {face.close();pose.close();bitmap.recycle()}
    }
}
