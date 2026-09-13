package com.photocoach.app.camera

import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.Semaphore
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger

/** Dedicated GL owner with one in-flight and one latest pending immutable frame.
 * A blocked native owner retains its global permit, so rebind cannot create unbounded threads.
 */
internal class MotionGlWorker(name:String,private val failed:(Throwable)->Unit,
    private val render:(MotionPixelPool.Lease)->Unit) {
    private val thread:HandlerThread
    private val handler:Handler
    private val closed=AtomicBoolean(false)
    private val pending=AtomicReference<MotionPixelPool.Lease?>()
    private val scheduled=AtomicBoolean(false)
    private val serial=AtomicLong()
    private val active=AtomicLong()
    private val failureReported=AtomicBoolean(false)
    private val controls=AtomicInteger()
    private val admission=Any()
    internal val terminated=java.util.concurrent.CountDownLatch(1)
    init {
        check(permits.tryAcquire()){ "previous GL owners have not released" }
        try {thread=object:HandlerThread(name) {
            override fun run() {try {super.run()} finally {permits.release();terminated.countDown()}}
        }.apply {start()};handler=Handler(thread.looper)}
        catch(error:Throwable){permits.release();throw error}
    }
    val executor=Executor {runnable->synchronized(admission) {
        if(closed.get())throw RejectedExecutionException("Live GL owner closed")
        if(controls.incrementAndGet()>8) {
            controls.decrementAndGet()
            val error=RejectedExecutionException("Live GL control queue quota exceeded");report(error);throw error
        }
        if(!handler.post {try {owned {runnable.run()}} finally {controls.decrementAndGet()}}) {
            controls.decrementAndGet();throw RejectedExecutionException("Live GL owner closed")
        }
    }}
    /** Reserved for the bounded, already-owned CameraX input/output acknowledgements and close. */
    val ownershipExecutor=Executor {runnable->synchronized(admission) {
        if(closed.get() || !handler.post {owned {runnable.run()}})
            throw RejectedExecutionException("Live GL ownership return after shutdown")
    }}
    fun submit(frame:MotionPixelPool.Lease) {
        if(closed.get()){frame.close();return}
        pending.getAndSet(frame)?.close()
        if(closed.get()){pending.getAndSet(null)?.close();return}
        schedule()
    }
    private fun schedule() {
        if(!scheduled.compareAndSet(false,true))return
        if(!handler.post {
            try {pending.getAndSet(null)?.use {frame->if(!closed.get())owned {render(frame)}}}
            finally {scheduled.set(false);if(pending.get()!=null && !closed.get())schedule()}
        }) {scheduled.set(false);pending.getAndSet(null)?.close();report(RejectedExecutionException("Live GL queue closed"))}
    }
    private fun owned(action:()->Unit) {
        val token=serial.incrementAndGet();active.set(token)
        val timeout=watch.schedule({if(active.get()==token)report(IllegalStateException("Live GL operation exceeded resource deadline"))},5,TimeUnit.SECONDS)
        try {action()}
        catch(error:Throwable){report(error)}
        finally {active.compareAndSet(token,0);timeout.cancel(false)}
    }
    fun runOnOwner(action:()->Unit) {
        check(android.os.Looper.myLooper()===thread.looper)
        owned(action)
    }
    private fun report(error:Throwable) {if(failureReported.compareAndSet(false,true))runCatching {failed(error)}}
    /** Caller supplies only this worker's resources; queued/in-flight frames release before cleanup. */
    fun close(cleanup:()->Unit) = synchronized(admission) {
        if(!closed.compareAndSet(false,true))return
        pending.getAndSet(null)?.close()
        check(handler.post {
            try {owned(cleanup)}
            finally {thread.quitSafely()}
        }){ "Live GL shutdown was rejected" }
    }
    companion object {
        // At most two three-owner candidate graphs, including native-stalled old graphs.
        private val permits=Semaphore(6)
        private val watch=ScheduledThreadPoolExecutor(1){r->Thread(r,"motion-gl-watch").apply {isDaemon=true}}
            .apply {removeOnCancelPolicy=true}
    }
}
