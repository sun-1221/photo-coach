package com.photocoach.app.camera

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** Three immutable published RGBA copies. Each sink owns its own buffer position and lease. */
internal class MotionPixelPool(val width:Int,val height:Int) {
    init {require(width>0 && height>0 && width.toLong()*height<=1280L*720)}
    private val slots=Array(3){Slot(ByteBuffer.allocateDirect(Math.multiplyExact(Math.multiplyExact(width,height),4)))}
    private class Slot(val bytes:ByteBuffer) {val references=AtomicInteger(0)}
    private val closed=AtomicBoolean(false)
    fun acquire():Frame? {
        if(closed.get())return null
        val slot=slots.firstOrNull {it.references.compareAndSet(0,1)} ?: return null
        if(closed.get()){slot.references.decrementAndGet();return null}
        return Frame(slot.bytes,slot.references)
    }
    fun close(){closed.set(true)} // Outstanding readers retain their bytes until they release.
    internal val outstanding:Int get()=slots.count {it.references.get()>0}
    internal class Frame(private val bytes:ByteBuffer,private val references:AtomicInteger) : AutoCloseable {
        private val released=AtomicBoolean(false)
        private var published=false
        fun writable():ByteBuffer {check(!published && !released.get());return bytes.apply {clear()}}
        fun publish(sensorNs:Long):Published {check(!published && !released.get());published=true;return Published(sensorNs,bytes,references,released)}
        override fun close(){synchronized(released){if(released.compareAndSet(false,true))check(references.decrementAndGet()>=0)}}
    }
    internal class Published(val sensorNs:Long,private val bytes:ByteBuffer,private val references:AtomicInteger,
        private val producerReleased:AtomicBoolean) {
        fun retain():Lease = synchronized(producerReleased) {
            check(!producerReleased.get()){ "producer lease already released" }
            check(references.incrementAndGet()>1)
            Lease(sensorNs,bytes.asReadOnlyBuffer().apply {clear()},references)
        }
    }
    internal class Lease(val sensorNs:Long,val pixels:ByteBuffer,private val references:AtomicInteger):AutoCloseable {
        private val released=AtomicBoolean(false)
        override fun close(){if(released.compareAndSet(false,true))check(references.decrementAndGet()>=0)}
    }
}
