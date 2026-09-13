package com.photocoach.app.camera

/** A mapping needs a measured sensor/media anchor, never the delivery time of Recorder.Start. */
data class MotionTimestampAnchor(val sensorUs: Long, val mediaUs: Long)

object MotionTimestampMapping {
    fun presentationUs(captureSensorUs: Long?, anchor: MotionTimestampAnchor?, firstMediaUs: Long, lastMediaUs: Long): Long? {
        if (captureSensorUs == null || anchor == null || lastMediaUs <= firstMediaUs) return null
        if (captureSensorUs < 0 || anchor.sensorUs < 0 || anchor.mediaUs < 0 || firstMediaUs < 0) return null
        val value = try { Math.addExact(anchor.mediaUs, Math.subtractExact(captureSensorUs, anchor.sensorUs)) }
            catch (_: ArithmeticException) { return null }
        return value.takeIf { it in firstMediaUs..lastMediaUs }
    }
}
