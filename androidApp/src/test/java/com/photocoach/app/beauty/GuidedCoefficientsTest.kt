package com.photocoach.app.beauty

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GuidedCoefficientsTest {
    @Test fun constantColorIsPreservedAcrossTheWholeImage() {
        for(value in listOf(0f,.02f,.4f,.8f,1f)) {
            val model=GuidedCoefficients.create(FloatArray(48) { value },8,6)
            for(y in 0..6) for(x in 0..8) assertEquals(value,model.reconstruct(x/8f,y/6f,value),.0001f)
        }
    }
    @Test fun edgeIsPreservedAndReconstructionIsBounded() {
        val data=FloatArray(128) { if(it%16<8) .1f else .9f }
        val model=GuidedCoefficients.create(data,16,8)
        assertTrue(model.reconstruct(.2f,.5f,.1f)<.12f)
        assertTrue(model.reconstruct(.8f,.5f,.9f)>.88f)
        for(y in 0..16) for(x in 0..32) assertTrue(model.reconstruct(x/32f,y/16f,.5f) in 0f..1f)
    }
}
