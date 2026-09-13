package com.photocoach.app.camera
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
class MotionTimestampBoundaryTest {
    @Test fun missingNegativeOverflowAndOutOfWindowAnchorsNeverBecomeValid() {
        assertNull(MotionTimestampMapping.presentationUs(10,null,0,100))
        assertNull(MotionTimestampMapping.presentationUs(null,MotionTimestampAnchor(0,0),0,100))
        assertNull(MotionTimestampMapping.presentationUs(-1,MotionTimestampAnchor(0,0),0,100))
        assertNull(MotionTimestampMapping.presentationUs(1,MotionTimestampAnchor(-1,0),0,100))
        assertNull(MotionTimestampMapping.presentationUs(1,MotionTimestampAnchor(0,-1),0,100))
        assertNull(MotionTimestampMapping.presentationUs(Long.MAX_VALUE,MotionTimestampAnchor(0,10),0,100))
        assertNull(MotionTimestampMapping.presentationUs(10,MotionTimestampAnchor(100,0),0,100))
        assertNull(MotionTimestampMapping.presentationUs(10,MotionTimestampAnchor(0,0),20,100))
        assertEquals(10L,MotionTimestampMapping.presentationUs(110,MotionTimestampAnchor(100,0),0,100))
    }
}
