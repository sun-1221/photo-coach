package com.photocoach.app.camera

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MotionGlWorkerTest {
    @Test fun captureResultAfterInputStopStillCorrelatesRetainedFrames() {
        val processor=MotionSurfaceProcessor {throw AssertionError(it)}
        val source=MotionSensorSource("0",1);val ledger=MotionFrameTimeline(1,source)
        for(name in listOf("timeline","correlationTimeline"))MotionSurfaceProcessor::class.java.getDeclaredField(name).apply {isAccessible=true}.set(processor,ledger)
        try {
            assertEquals(0L,ledger.submit(1_000_000_000L));assertEquals(0L,ledger.encoded(0L,true,100))
            assertEquals(2_000_000L,ledger.submit(3_000_000_000L));assertEquals(2_000_000L,ledger.encoded(2_000_000L,false,100))
            processor.finish()
            val stopped=CountDownLatch(1);processor.executor.execute {stopped.countDown()}
            assertTrue(stopped.await(2,TimeUnit.SECONDS))
            processor.observe(MotionSensorSource("0",2),1_000_000_000L,1)
            assertNull(ledger.failure)
            processor.observe(source,1_000_000_000L,1);processor.observe(source,3_000_000_000L,2)
            assertNotNull(ledger.verifyMux(listOf(MuxedVideoSample(0,true),MuxedVideoSample(2_000_000,false))))
        } finally {processor.close();processor.ownerTerminations.forEach {assertTrue(it.await(3,TimeUnit.SECONDS))}}
    }
    @Test fun surfaceFinishRegistersBudgetBeforeBlockedEncoderOwnerCanCleanUp() {
        val now=android.os.SystemClock.elapsedRealtime()
        val budget=MotionSessionBudget(now);assertTrue(budget.ready(now))
        val processor=MotionSurfaceProcessor {throw AssertionError(it)}
        MotionSurfaceProcessor::class.java.getDeclaredField("budget").apply {isAccessible=true}.set(processor,budget)
        val worker=MotionSurfaceProcessor::class.java.getDeclaredField("encoderWorker").apply {isAccessible=true}.get(processor) as MotionGlWorker
        val entered=CountDownLatch(1);val release=CountDownLatch(1)
        try {
            worker.executor.execute {entered.countDown();check(release.await(3,TimeUnit.SECONDS))}
            assertTrue(entered.await(2,TimeUnit.SECONDS))
            val requestedAt=android.os.SystemClock.elapsedRealtime()
            processor.finish()
            val deadline=budget.deadlineMs()
            assertTrue(deadline in requestedAt+12000..android.os.SystemClock.elapsedRealtime()+12000)
            assertEquals(1L,release.count)
            release.countDown();processor.close()
            processor.ownerTerminations.forEach {assertTrue(it.await(3,TimeUnit.SECONDS))}
            assertEquals(deadline,budget.deadlineMs())
        } finally {release.countDown();processor.close()}
    }
    @Test fun deadlineReportsWhileBlockedOwnerKeepsItsPermitUntilCleanupFinishes() {
        val entered=CountDownLatch(1);val release=CountDownLatch(1);val failure=CountDownLatch(1)
        val worker=MotionGlWorker("motion-stalled-owner",{failure.countDown()},{})
        val others=mutableListOf<MotionGlWorker>()
        try {
            worker.executor.execute {entered.countDown();check(release.await(8,TimeUnit.SECONDS))}
            assertTrue(entered.await(2,TimeUnit.SECONDS))
            repeat(5){others+=MotionGlWorker("motion-quota-$it",{},{})}
            worker.close {}
            assertTrue(failure.await(6,TimeUnit.SECONDS))
            assertEquals(1L,worker.terminated.count)
            assertTrue(runCatching {MotionGlWorker("motion-over-quota",{},{})}.isFailure)
            release.countDown();assertTrue(worker.terminated.await(2,TimeUnit.SECONDS))
            val replacement=MotionGlWorker("motion-after-ack",{},{})
            replacement.close {};assertTrue(replacement.terminated.await(2,TimeUnit.SECONDS))
        } finally {
            release.countDown();worker.close {};others.forEach {it.close {}}
            assertTrue(worker.terminated.await(2,TimeUnit.SECONDS))
            assertTrue(others.all {it.terminated.await(2,TimeUnit.SECONDS)})
        }
    }
    @Test fun concurrentAdmissionAndCloseNeverRunControlAfterCleanup() {
        repeat(40) {
            val destroyed=AtomicBoolean(false)
            val afterCleanup=AtomicInteger()
            val accepted=AtomicInteger()
            val ran=AtomicInteger()
            val worker=MotionGlWorker("motion-close-test",{throw AssertionError(it)},{})
            val start=CountDownLatch(1)
            val sender=Thread {
                start.await()
                try {
                    worker.executor.execute {if(destroyed.get())afterCleanup.incrementAndGet();ran.incrementAndGet()}
                    accepted.incrementAndGet()
                } catch(_:RejectedExecutionException) { }
            }
            val closer=Thread {start.await();worker.close {assertTrue(destroyed.compareAndSet(false,true))}}
            sender.start();closer.start();start.countDown()
            sender.join(2000);closer.join(2000)
            assertFalse(sender.isAlive);assertFalse(closer.isAlive)
            assertTrue(worker.terminated.await(2,TimeUnit.SECONDS))
            assertTrue(destroyed.get());assertEquals(0,afterCleanup.get());assertEquals(accepted.get(),ran.get())
            assertTrue(runCatching {worker.executor.execute {error("closed worker accepted task")}}.exceptionOrNull() is RejectedExecutionException)
        }
    }
    @Test fun closeWaitsForInflightFrameBeforeCleanupAndReleasesAllLeases() {
        val entered=CountDownLatch(1);val release=CountDownLatch(1)
        val rendered=AtomicBoolean(false);val cleaned=AtomicBoolean(false)
        val pool=MotionPixelPool(2,2)
        val worker=MotionGlWorker("motion-frame-close-test",{throw AssertionError(it)},{
            entered.countDown();check(release.await(2,TimeUnit.SECONDS));rendered.set(true)
        })
        pool.acquire()!!.use {worker.submit(it.publish(1).retain())}
        assertTrue(entered.await(2,TimeUnit.SECONDS))
        pool.acquire()!!.use {worker.submit(it.publish(2).retain())}
        worker.close {check(rendered.get());cleaned.set(true)}
        assertFalse(cleaned.get());release.countDown()
        assertTrue(worker.terminated.await(2,TimeUnit.SECONDS));assertTrue(cleaned.get())
        assertEquals(0,pool.outstanding);pool.close()
    }
}
