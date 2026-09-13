package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MotionPixelPoolTest {
    @Test fun independentSinkLeasesPreventReuseAndHaveIndependentPositions() {
        val pool=MotionPixelPool(2,2)
        val producer=pool.acquire()!!
        producer.writable().put(byteArrayOf(1,2,3,4))
        val published=producer.publish(99)
        val recorder=published.retain();val ownEncoder=published.retain()
        producer.close()
        assertEquals(1,recorder.pixels.get(0).toInt())
        recorder.pixels.position(3)
        assertEquals(0,ownEncoder.pixels.position())
        assertTrue(ownEncoder.pixels.isReadOnly)
        val second=pool.acquire()!!;val third=pool.acquire()!!
        assertNull(pool.acquire())
        recorder.close();assertNull(pool.acquire())
        ownEncoder.close();assertNotNull(pool.acquire()?.also {it.close()})
        assertThrows(IllegalStateException::class.java){published.retain()}
        second.close();third.close();assertEquals(0,pool.outstanding)
    }
    @Test fun closeRejectsNewWorkButDoesNotInvalidateSlowConsumerBytes() {
        val pool=MotionPixelPool(1,1);val frame=pool.acquire()!!
        frame.writable().putInt(0,42)
        val lease=frame.publish(1).retain();frame.close();pool.close()
        assertNull(pool.acquire());assertEquals(42,lease.pixels.getInt(0));assertEquals(1,pool.outstanding)
        lease.close();lease.close();assertEquals(0,pool.outstanding)
    }
    @Test fun dimensionsStayWithinHdPixelAndFixedSlotQuota() {
        assertThrows(IllegalArgumentException::class.java){MotionPixelPool(1920,1080)}
        assertThrows(IllegalArgumentException::class.java){MotionPixelPool(0,720)}
        val pool=MotionPixelPool(720,1280)
        val frames=List(3){pool.acquire()!!}
        assertNull(pool.acquire());frames.forEach {it.close()};pool.close()
    }
}
