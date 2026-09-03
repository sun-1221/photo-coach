package com.photocoach.app.beauty

import com.photocoach.app.camera.CameraUserSettings
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BeautyModelTest {
    private val landmarks = BeautyLandmarks(BeautyPoint(30f,40f), BeautyPoint(70f,40f),
        BeautyPoint(50f,61f), BeautyPoint(50f,76f), BeautyPoint(39f,72f), BeautyPoint(61f,72f),
        BeautyPoint(28f,61f), BeautyPoint(72f,61f))
    private val mask get() = requireNotNull(BeautyMaskFactory.create(100f,100f,0f,landmarks))
    private fun frame(ms:Long, count:Int = 1) = BeautyFaceFrame(ms*1_000_000,count,BeautyTransform(),mask)

    @Test fun defaultsAndPresetCapsAreConservative() {
        assertEquals(BeautyPreset.OFF,CameraUserSettings.DEFAULT.beautyPreset)
        assertEquals(BeautyPreset.OFF,BeautyPreset.restore(null))
        assertEquals(BeautyPreset.OFF,BeautyPreset.restore("unknown"))
        assertEquals(BeautyPreset.SOFT,BeautyPreset.restore("SOFT"))
        BeautyPreset.entries.forEach { assertTrue(it.blend in 0f.. .28f); assertTrue(it.detail in .6f..1f) }
        assertThrows(IllegalArgumentException::class.java) { BeautyPreset.requireSupported("NATURAL",2) }
        assertThrows(IllegalStateException::class.java) { BeautyPreset.requireSupported("future",1) }
    }

    @Test fun onlyStandardPhotoAllowsBeautyAndOffNeverBlocksOtherModes() {
        assertNull(BeautyCompatibilityPolicy.rejection(BeautyPreset.OFF,true,false))
        assertNull(BeautyCompatibilityPolicy.rejection(BeautyPreset.NATURAL,false,true))
        assertNotNull(BeautyCompatibilityPolicy.rejection(BeautyPreset.SOFT,true,true))
        assertNotNull(BeautyCompatibilityPolicy.rejection(BeautyPreset.NATURAL,false,false))
    }

    @Test fun originalIsDefaultEvenWithBeautyAndAutoSaveAddsOneDerivative() {
        BeautyPreset.entries.forEach { assertFalse(BeautyCompatibilityPolicy.needsDerivative(it,false,false)) }
        assertFalse(BeautyCompatibilityPolicy.needsDerivative(BeautyPreset.OFF,true,true))
        assertTrue(BeautyCompatibilityPolicy.needsDerivative(BeautyPreset.NATURAL,true,true))
        assertTrue(BeautyCompatibilityPolicy.needsDerivative(BeautyPreset.OFF,true,false))
    }

    @Test fun geometryProtectsFeaturesAndBackgroundWithoutSkinColorRules() {
        listOf(landmarks.leftEye,landmarks.rightEye,landmarks.nose,landmarks.mouth).forEach {
            assertEquals(0f,mask.weight(it),1e-5f)
        }
        assertTrue(mask.weight(landmarks.leftCheek) > .9f)
        assertTrue(mask.weight(landmarks.rightCheek) > .9f)
        assertEquals(0f,mask.weight(BeautyPoint(-5f,50f)))
        assertEquals(0f,mask.weight(BeautyPoint(0f,0f)))
        for(y in 0..100) for(x in 0..100) assertTrue(mask.weight(x.toFloat(),y.toFloat()) in 0f..1f)
    }

    @Test fun missingSmallSidewaysAndInvalidLandmarksDisableMask() {
        assertNull(BeautyMaskFactory.create(100f,100f,0f,null))
        assertNull(BeautyMaskFactory.create(20f,100f,0f,landmarks))
        assertNull(BeautyMaskFactory.create(100f,100f,36f,landmarks))
        assertNull(BeautyMaskFactory.create(100f,100f,Float.NaN,landmarks))
        assertNull(BeautyMaskFactory.create(100f,100f,0f,landmarks.copy(nose=BeautyPoint(Float.NaN,1f))))
        assertNull(BeautyMaskFactory.create(100f,100f,0f,landmarks.copy(mouth=BeautyPoint(50f,20f))))
    }

    @Test fun maskFollowsRotatedAndTranslatedLandmarks() {
        val transform = BeautyTransform(0.8660254f,-.5f,90f,.5f,.8660254f,20f)
        fun p(p:BeautyPoint) = transform.map(p)
        val rotated = landmarks.let { BeautyLandmarks(p(it.leftEye),p(it.rightEye),p(it.nose),p(it.mouth),
            p(it.leftMouth),p(it.rightMouth),p(it.leftCheek),p(it.rightCheek)) }
        val result = requireNotNull(BeautyMaskFactory.create(100f,100f,0f,rotated))
        assertEquals(mask.weight(landmarks.leftCheek),result.weight(p(landmarks.leftCheek)),.001f)
        assertEquals(0f,result.weight(p(landmarks.leftEye)),.001f)
    }

    @Test fun latestOnlyStoreRejectsOldFramesAndSuppressesMultipleFacesImmediately() {
        val store = BeautyFaceStore()
        store.publish(frame(100)); store.publish(frame(200,2)); store.publish(frame(150))
        assertEquals(200_000_000L,store.snapshot()?.timestampNs)
        assertNull(store.snapshot()?.mask)
        store.publish(frame(210,0)); assertNull(store.snapshot()?.mask)
        store.publish(frame(300)); assertNotNull(store.snapshot()?.mask)
        store.clear(); assertNull(store.snapshot())
    }

    @Test fun freshnessIsZeroForFutureAndExpiredFrames() {
        assertEquals(1f,BeautyFaceStore.freshness(0,150_000_000))
        assertEquals(.5f,BeautyFaceStore.freshness(0,225_000_000))
        assertEquals(0f,BeautyFaceStore.freshness(0,300_000_000))
        assertEquals(0f,BeautyFaceStore.freshness(2_000_000,1_000_000))
    }

    @Test fun smoothingResetsOnLargeMotionAndSensorChanges() {
        val store=BeautyFaceStore(); val first=frame(100)
        store.publish(first)
        val small=frame(110).copy(mask=mask.copy(analysisToLocal=mask.analysisToLocal.copy(c=.02f)))
        store.publish(small)
        assertNotEquals(small.mask,store.snapshot()?.mask)
        val large=frame(120).copy(mask=mask.copy(analysisToLocal=mask.analysisToLocal.copy(c=20f)))
        store.publish(large); assertEquals(large,store.snapshot())
        val sensor=frame(130).copy(sensorToAnalysis=BeautyTransform(a=2f,e=2f))
        store.publish(sensor); assertEquals(sensor,store.snapshot())
    }

    @Test fun abruptRotationAndZoomNeverInterpolateThroughASingularMask() {
        val first=frame(100)
        for(change in listOf(BeautyTransform(-1f,0f,100f,0f,-1f,100f),
            BeautyTransform(2f,0f,-50f,0f,2f,-50f))) {
            val store=BeautyFaceStore(); store.publish(first)
            val next=frame(125).copy(mask=mask.copy(analysisToLocal=mask.analysisToLocal*change))
            store.publish(next)
            assertEquals(next,store.snapshot())
            assertNotNull(store.snapshot()?.mask?.analysisToLocal?.inverse())
        }
    }
}
