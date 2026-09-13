package com.photocoach.app.camera

/** Identity belongs to one Camera2 graph, not a wall-clock or Recorder event. */
internal data class MotionSensorSource(val cameraId:String,val generation:Long) {
    init {require(cameraId.isNotBlank() && generation>0)}
}
internal data class MuxedVideoSample(val ptsUs:Long,val keyFrame:Boolean)
internal data class RetainedMotionFrame(val sensorNs:Long,val submittedPtsUs:Long,val muxPtsUs:Long,val keyFrame:Boolean)
internal data class WrittenMotionSample(val sensorNs:Long,val ptsUs:Long,val keyFrame:Boolean)
internal data class VerifiedMotionTimeline(val recordingGeneration:Long,val source:MotionSensorSource,
    val frames:List<RetainedMotionFrame>) {
    val durationUs:Long get()=frames.last().muxPtsUs
    /** Interpolate only between two actual, sensor-correlated retained samples. Never clamp. */
    fun presentationUs(captureSource:MotionSensorSource,captureSensorNs:Long):Long? {
        if(captureSource!=source || frames.isEmpty() || captureSensorNs !in frames.first().sensorNs..frames.last().sensorNs)return null
        frames.firstOrNull {it.sensorNs==captureSensorNs}?.let {return it.muxPtsUs}
        val right=frames.indexOfFirst {it.sensorNs>captureSensorNs}
        if(right<=0)return null
        val a=frames[right-1];val b=frames[right]
        return runCatching {Math.addExact(a.muxPtsUs,Math.multiplyExact(captureSensorNs-a.sensorNs,b.muxPtsUs-a.muxPtsUs)/(b.sensorNs-a.sensorNs))}.getOrNull()
    }
    fun fullWindow(captureSource:MotionSensorSource,captureSensorNs:Long):MotionClipWindow? {
        val at=presentationUs(captureSource,captureSensorNs) ?: return null
        val before=runCatching {Math.subtractExact(captureSensorNs,MotionClipWindow.SIDE_US*1000L)}.getOrNull() ?: return null
        val after=runCatching {Math.addExact(captureSensorNs,MotionClipWindow.SIDE_US*1000L)}.getOrNull() ?: return null
        val start=presentationUs(captureSource,before) ?: return null
        val end=presentationUs(captureSource,after) ?: return null
        if(start>=end)return null
        return MotionClipWindow(start,end,at-start)
    }
}

/** Bounded input→actual codec output→actual mux sample ledger. All calls may cross owner threads. */
internal class MotionFrameTimeline(val generation:Long,val source:MotionSensorSource) {
    init {require(generation>0)}
    private data class Input(val sensorNs:Long,var correlated:Boolean)
    private data class Written(val input:Input,val ptsUs:Long,val keyFrame:Boolean)
    private val results=linkedMapOf<Long,Long>()
    private val inputs=linkedMapOf<Long,Input>()
    private val written=mutableListOf<Written>()
    private var originSensorNs:Long?=null
    private var lastInputNs:Long?=null
    private var firstKeptPtsUs:Long?=null
    private var lastOutputPtsUs:Long?=null
    private var bytes=0L
    private var sealed=false
    var failure:String?=null;private set
    @Synchronized fun writtenSamples():List<WrittenMotionSample> = written.map {WrittenMotionSample(it.input.sensorNs,it.ptsUs,it.keyFrame)}
    /** Admission check: stop the producer before submitting a frame outside the bounded segment. */
    @Synchronized fun exceedsInputDuration(sensorNs:Long):Boolean = originSensorNs?.let {origin ->
        runCatching {Math.subtractExact(sensorNs,origin)>MotionTemporaryPolicy.MAX_RECORDING_DURATION_MS*1_000_000L}.getOrDefault(false)
    } ?: false
    @Synchronized fun fail(reason:String) {if(failure==null)failure=reason}
    @Synchronized fun sensorResult(identity:MotionSensorSource,sensorNs:Long,frameNumber:Long) {
        if(sealed || failure!=null)return
        if(identity!=source || sensorNs<=0 || frameNumber<0){fail("sensor result identity is not verified");return}
        val previous=results[sensorNs]
        if(previous!=null && previous!=frameNumber){fail("ambiguous sensor timestamp");return}
        results[sensorNs]=frameNumber
        inputs.values.firstOrNull {it.sensorNs==sensorNs}?.correlated=true
        while(results.size>MAX_FRAMES)results.remove(results.keys.first())
    }
    @Synchronized fun submit(sensorNs:Long):Long? {
        if(sealed || failure!=null)return null
        if(sensorNs<=0){fail("missing input timestamp");return null}
        if(lastInputNs==sensorNs)return null // Coalesced SurfaceTexture callbacks cannot manufacture another frame.
        if(lastInputNs?.let {sensorNs<it}==true){fail("input timestamp moved backwards");return null}
        if(inputs.size>=MAX_FRAMES){fail("frame ledger quota exceeded");return null}
        val origin=originSensorNs ?: sensorNs.also {originSensorNs=it}
        val pts=runCatching {Math.subtractExact(sensorNs,origin)/1000L}.getOrNull()
        if(pts==null || pts<0 || pts>MotionTemporaryPolicy.MAX_RECORDING_DURATION_MS*1000L || pts in inputs){fail("invalid input PTS");return null}
        lastInputNs=sensorNs;inputs[pts]=Input(sensorNs,sensorNs in results)
        return pts
    }
    /** Caller invokes only for nonempty, non-codec-config buffers; first non-key samples are discarded. */
    @Synchronized fun encoded(ptsUs:Long,keyFrame:Boolean,size:Int):Long? {
        if(sealed || failure!=null)return null
        if(size<=0)return null
        if(ptsUs !in inputs || lastOutputPtsUs?.let {ptsUs<=it}==true){fail("unmatched or reordered codec output");return null}
        lastOutputPtsUs=ptsUs
        if(firstKeptPtsUs==null && !keyFrame)return null
        val first=firstKeptPtsUs ?: ptsUs.also {firstKeptPtsUs=it}
        if(bytes+size>MotionTemporaryPolicy.MAX_RECORDING_BYTES){fail("encoded byte quota exceeded");return null}
        bytes+=size
        written+=Written(inputs.getValue(ptsUs),ptsUs,keyFrame)
        return ptsUs-first
    }
    /** Availability is based on retained codec samples; final coverage must still use verified mux times. */
    @Synchronized fun hasPrebuffer():Boolean {
        if(failure!=null)return false
        val correlated=written.filter {it.input.correlated}
        if(correlated.size<2)return false
        val spanUs=(correlated.last().input.sensorNs-correlated.first().input.sensorNs)/1000L
        if(spanUs !in MotionClipWindow.SIDE_US..(MotionTemporaryPolicy.MAX_RECORDING_DURATION_MS*1000L-MotionClipWindow.SIDE_US))
            return false
        val origin=originSensorNs ?: return false
        val last=lastInputNs ?: return false
        val remainingUs=MotionTemporaryPolicy.MAX_RECORDING_DURATION_MS*1000L-(last-origin)/1000L
        // A 1.5s suffix is not "ready" if the 8s media span cannot still hold the matching postbuffer.
        return remainingUs>=MotionClipWindow.SIDE_US
    }
    @Synchronized fun hasPostbuffer(identity:MotionSensorSource,captureSensorNs:Long):Boolean {
        if(identity!=source || failure!=null || captureSensorNs<=0)return false
        val end=runCatching {Math.addExact(captureSensorNs,MotionClipWindow.SIDE_US*1000L)}.getOrNull() ?: return false
        return written.lastOrNull()?.let {it.input.correlated && it.input.sensorNs>=end}==true
    }
    @Synchronized fun verifyMux(samples:List<MuxedVideoSample>,trackTimescale:Long?=null,tables:MotionTimeTables?=null):VerifiedMotionTimeline? {
        sealed=true
        fun reject(reason:String):VerifiedMotionTimeline? {fail(reason);return null}
        if(failure!=null)return null
        if(samples.size!=written.size || samples.size<2 || samples.size>MAX_FRAMES)
            return reject("mux sample count=${samples.size}, written=${written.size}")
        if(samples.first().ptsUs!=0L || !samples.first().keyFrame)return reject("mux first sample=${samples.first()}")
        if(written.any {!it.input.correlated})return reject("retained input has no exact sensor result: ${written.filter {!it.input.correlated}.take(8).map {it.input.sensorNs}}")
        if(samples.zipWithNext().any {(a,b)->b.ptsUs<=a.ptsUs} || samples.last().ptsUs>MotionTemporaryPolicy.MAX_RECORDING_DURATION_MS*1000L)
            return reject("mux time order or duration invalid")
        if(samples.indices.any {samples[it].keyFrame!=written[it].keyFrame})return reject("mux keyframe flags differ")
        if(trackTimescale!=null && trackTimescale !in 1L..Int.MAX_VALUE.toLong())return reject("invalid track timescale")
        // A measured mdhd timescale permits at most one track tick plus the extractor's
        // sub-microsecond integer conversion. Without that evidence, require exact PTS.
        val roundingUs=trackTimescale?.let {(1_000_000L+it-1)/it+1} ?: 0L
        val origin=written.first().ptsUs
        if(tables!=null) {
            if(tables.timescale!=trackTimescale || !MotionMuxTimingModel.matches(written.map {it.ptsUs-origin},InspectedMotionMp4(tables,samples)))
                return reject("mux tables do not match the written-sample integer model")
        } else {
            for(index in samples.indices) {
                val expected=written[index].ptsUs-origin
                if(kotlin.math.abs(samples[index].ptsUs-expected)>roundingUs)
                    return reject("mux PTS discrepancy at $index: actual=${samples[index].ptsUs}, expected=$expected, timescale=$trackTimescale")
            }
        }
        // The muxer can quantize track times. Keep its actual values, not assumed submitted values.
        return VerifiedMotionTimeline(generation,source,samples.indices.map {i ->
            RetainedMotionFrame(written[i].input.sensorNs,written[i].ptsUs,samples[i].ptsUs,samples[i].keyFrame)
        })
    }
    companion object {const val MAX_FRAMES=512}
}
