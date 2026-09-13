package com.photocoach.app.camera

import java.util.concurrent.Semaphore

/** Main-thread owner. Delivering our MP4 or asking Recorder to stop does not retire its lease. */
internal class LiveRecorderOwnership(private val permits:Semaphore=processPermits) {
    private var token:Long?=null
    val canStart:Boolean get()=token==null
    fun acquire(generation:Long):Boolean {
        if(!canStart || !permits.tryAcquire())return false
        token=generation;return true
    }
    fun finalized(generation:Long):Boolean {
        if(token!=generation)return false
        token=null;permits.release();return true
    }
    companion object {
        private val processPermits=Semaphore(2)
        fun canCreateCandidate()=processPermits.availablePermits()>0
    }
}
