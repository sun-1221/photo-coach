package com.photocoach.app.beauty

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BeautyTransformTest {
    @Test fun rotationCornersHaveExpectedBounds() {
        val p=BeautyPoint(40f,30f)
        assertPoint(BeautyPoint(40f,30f),BeautyTransform.rotation(400,300,0).map(p))
        assertPoint(BeautyPoint(270f,40f),BeautyTransform.rotation(400,300,90).map(p))
        assertPoint(BeautyPoint(360f,270f),BeautyTransform.rotation(400,300,180).map(p))
        assertPoint(BeautyPoint(30f,360f),BeautyTransform.rotation(400,300,270).map(p))
    }
    @Test fun croppedMirroredOutputsMapBackToAnalysisForAllRotationsAndRatios() {
        val sensorPoint=BeautyPoint(1300f,1100f)
        val sensorToBuffer=BeautyTransform(.16f,0f,-16f,0f,.16f,-32f)
        for(rotation in listOf(0,90,180,270)) for(aspect in listOf(4f/3,16f/9)) for(mirror in listOf(false,true)) {
            val sensorToAnalysis=BeautyTransform.rotation(640,480,rotation)*sensorToBuffer
            val sensorToOutput=BeautyTransform(if(mirror) -.4f else .4f,0f,if(mirror) 1600f else -40f,
                0f,.4f/aspect,-80f)
            val outputToAnalysis=sensorToAnalysis*requireNotNull(sensorToOutput.inverse())
            assertPoint(sensorToAnalysis.map(sensorPoint),outputToAnalysis.map(sensorToOutput.map(sensorPoint)))
        }
    }
    @Test fun invalidAndSingularMatricesAreRejected() {
        assertNull(BeautyTransform(a=0f,e=0f).inverse())
        assertNull(BeautyTransform(c=Float.NaN).inverse())
        assertNull(BeautyTransform(a=Float.MAX_VALUE,e=Float.MAX_VALUE).inverse())
        assertThrows(IllegalStateException::class.java) { BeautyTransform.rotation(4,3,45) }
    }
    private fun assertPoint(expected:BeautyPoint,actual:BeautyPoint) {
        assertEquals(expected.x,actual.x,.001f); assertEquals(expected.y,actual.y,.001f)
    }
}
