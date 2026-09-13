package com.photocoach.app.camera

import java.util.concurrent.Semaphore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LiveRecorderOwnershipTest {
    @Test fun ownVideoMayFinishWhileLateRecorderStillBlocksRestartUntilItsFinalize() {
        val capacity=Semaphore(2);val owner=LiveRecorderOwnership(capacity)
        assertTrue(owner.acquire(1))
        // Own video delivery has no call into this independent Recorder ownership registry.
        assertFalse(owner.canStart);assertFalse(owner.acquire(2));assertEquals(1,capacity.availablePermits())
        assertFalse(owner.finalized(0));assertFalse(owner.canStart)
        assertTrue(owner.finalized(1));assertTrue(owner.canStart)
        assertTrue(owner.acquire(2));assertFalse(owner.finalized(1));assertFalse(owner.canStart)
        assertTrue(owner.finalized(2));assertEquals(2,capacity.availablePermits())
        assertFalse(owner.finalized(2));assertEquals(2,capacity.availablePermits())
    }
    @Test fun twoRetiredControllersBoundProcessQuotaAndCannotReleaseEachOthersOwner() {
        val capacity=Semaphore(2)
        val a=LiveRecorderOwnership(capacity);val b=LiveRecorderOwnership(capacity);val c=LiveRecorderOwnership(capacity)
        assertTrue(a.acquire(10));assertTrue(b.acquire(20));assertFalse(c.acquire(30))
        assertFalse(a.finalized(20));assertEquals(0,capacity.availablePermits())
        assertTrue(a.finalized(10));assertTrue(c.acquire(30))
        assertFalse(a.finalized(10));assertFalse(b.canStart);assertFalse(c.canStart)
        assertTrue(b.finalized(20));assertTrue(c.finalized(30));assertEquals(2,capacity.availablePermits())
    }
}
