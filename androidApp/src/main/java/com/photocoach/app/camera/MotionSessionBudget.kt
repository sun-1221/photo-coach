package com.photocoach.app.camera

/** Resource timing only; never used as a sensor or media timestamp. */
internal class MotionSessionBudget(private val startedAtMs:Long) {
    init {require(startedAtMs in 0..Long.MAX_VALUE-32_000L)}
    private var readyAtMs:Long?=null
    private var finishingAtMs:Long?=null
    @Synchronized fun ready(nowMs:Long):Boolean {
        if(readyAtMs!=null)return false
        if(nowMs<startedAtMs || nowMs>=startedAtMs+12_000L)return false
        readyAtMs=nowMs
        return true
    }
    @Synchronized fun finishing(nowMs:Long) {
        val ready=readyAtMs ?: return
        if(finishingAtMs==null)finishingAtMs=nowMs.coerceIn(ready,ready+8_000L)
    }
    @Synchronized fun hasReady()=readyAtMs!=null
    @Synchronized fun stopDelay(nowMs:Long):Long = ((readyAtMs ?: error("encoder not ready"))+8_000L-nowMs).coerceAtLeast(0L)
    @Synchronized fun deadlineMs():Long = readyAtMs?.let {ready ->
        minOf(ready+20_000L,finishingAtMs?.plus(12_000L) ?: Long.MAX_VALUE)
    } ?: (startedAtMs+12_000L)
    @Synchronized fun expired(nowMs:Long)=nowMs>=deadlineMs()
}
