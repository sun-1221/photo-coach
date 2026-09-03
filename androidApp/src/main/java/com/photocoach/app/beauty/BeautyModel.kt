package com.photocoach.app.beauty

import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot

enum class BeautyPreset(val label: String, val blend: Float, val detail: Float) {
    OFF("关闭", 0f, 1f), NATURAL("自然", 0.18f, 0.70f), SOFT("柔和", 0.28f, 0.60f);
    companion object {
        const val ENGINE_VERSION = 1
        fun restore(value: String?): BeautyPreset = entries.firstOrNull { it.name == value } ?: OFF
        fun requireSupported(value: String, version: Int): BeautyPreset {
            require(version == ENGINE_VERSION) { "美颜引擎版本不支持，原片已保留" }
            return entries.firstOrNull { it.name == value } ?: error("美颜配方不支持，原片已保留")
        }
    }
}

data class BeautyPoint(val x: Float, val y: Float)

/** Immutable affine matrix: Android pixels (top-left origin), never GL texture coordinates. */
data class BeautyTransform(val a: Float = 1f, val b: Float = 0f, val c: Float = 0f,
    val d: Float = 0f, val e: Float = 1f, val f: Float = 0f) {
    fun map(p: BeautyPoint) = BeautyPoint(a * p.x + b * p.y + c, d * p.x + e * p.y + f)
    operator fun times(r: BeautyTransform) = BeautyTransform(
        a*r.a+b*r.d, a*r.b+b*r.e, a*r.c+b*r.f+c,
        d*r.a+e*r.d, d*r.b+e*r.e, d*r.c+e*r.f+f)
    fun inverse(): BeautyTransform? {
        val det = a*e-b*d
        if (!listOf(a,b,c,d,e,f).all(Float::isFinite) || !det.isFinite() || abs(det) < 1e-10f) return null
        return BeautyTransform(e/det, -b/det, (b*f-e*c)/det, -d/det, a/det, (d*c-a*f)/det)
            .takeIf { listOf(it.a,it.b,it.c,it.d,it.e,it.f).all(Float::isFinite) }
    }
    fun glValues() = floatArrayOf(a,d,0f,b,e,0f,c,f,1f)
    companion object {
        fun rotation(width: Int, height: Int, degrees: Int): BeautyTransform = when (degrees.mod(360)) {
            0 -> BeautyTransform()
            90 -> BeautyTransform(0f,-1f,height.toFloat(),1f,0f,0f)
            180 -> BeautyTransform(-1f,0f,width.toFloat(),0f,-1f,height.toFloat())
            270 -> BeautyTransform(0f,1f,0f,-1f,0f,width.toFloat())
            else -> error("Only right-angle camera rotations are supported")
        }
    }
}

data class BeautyEllipse(val x: Float, val y: Float, val rx: Float, val ry: Float) {
    init { require(listOf(x, y, rx, ry).all(Float::isFinite) && rx > 0f && ry > 0f) }
    fun weight(p: BeautyPoint): Float {
        return weight(p.x, p.y)
    }
    fun weight(px: Float, py: Float): Float {
        if (abs(px-x) >= rx || abs(py-y) >= ry) return 0f
        val radius = hypot((px-x)/rx, (py-y)/ry)
        val t = ((1f-radius)/0.30f).coerceIn(0f,1f)
        return t*t*(3f-2f*t)
    }
}

data class BeautyMask(val analysisToLocal: BeautyTransform,
    val skin: List<BeautyEllipse>, val protected: List<BeautyEllipse>) {
    init { require(skin.size == 4 && protected.size == 4 && analysisToLocal.inverse() != null) }
    fun weight(point: BeautyPoint): Float = weight(point.x, point.y)
    fun weight(x: Float, y: Float): Float {
        val px = analysisToLocal.a*x + analysisToLocal.b*y + analysisToLocal.c
        val py = analysisToLocal.d*x + analysisToLocal.e*y + analysisToLocal.f
        var weight = 0f
        for (ellipse in skin) weight = maxOf(weight, ellipse.weight(px, py))
        if (weight == 0f) return 0f
        var protection = 0f
        for (ellipse in protected) protection = maxOf(protection, ellipse.weight(px, py))
        return weight*(1f-protection)
    }
}

data class BeautyFaceFrame(val timestampNs: Long, val faceCount: Int,
    val sensorToAnalysis: BeautyTransform, val mask: BeautyMask?)

/** Capacity one, no face IDs, image retention, logging or persistent facial data. */
class BeautyFaceStore {
    private val latest = AtomicReference<BeautyFaceFrame?>()
    fun clear() { latest.set(null) }
    fun publish(frame: BeautyFaceFrame) {
        val safeFrame = if (frame.faceCount == 1) frame else frame.copy(mask = null)
        latest.updateAndGet { previous ->
            if (previous != null && safeFrame.timestampNs < previous.timestampNs) previous else smooth(previous, safeFrame)
        }
    }
    fun snapshot() = latest.get()
    private fun smooth(old: BeautyFaceFrame?, next: BeautyFaceFrame): BeautyFaceFrame {
        val previous = old?.mask ?: return next
        val current = next.mask ?: return next
        val dt = (next.timestampNs - old.timestampNs) / 1_000_000f
        if (dt <= 0 || dt > 300 || old.sensorToAnalysis != next.sensorToAnalysis) return next
        // Do not drag an old face mask across a cut or rapid subject replacement.
        val center = current.analysisToLocal.inverse()?.map(BeautyPoint(.5f,.5f)) ?: return next
        val delta = previous.analysisToLocal.map(center)
        if (hypot(delta.x-.5f,delta.y-.5f) > .18f) return next
        val a = previous.analysisToLocal; val b = current.analysisToLocal
        val oldScale = hypot(a.a,a.b); val newScale = hypot(b.a,b.b)
        val ratio = newScale/oldScale
        val direction = (a.a*b.a+a.b*b.b)/(oldScale*newScale)
        // Do not interpolate opposing rotations or abrupt zoom changes through a singular matrix.
        if (!ratio.isFinite() || !direction.isFinite() || ratio !in .67f..1.5f || direction < .7071f) return next
        val t = (1f-exp(-dt/35f)).coerceIn(.25f,1f)
        fun mix(x: Float, y: Float) = x+(y-x)*t
        return next.copy(mask = current.copy(analysisToLocal = BeautyTransform(
            mix(a.a,b.a),mix(a.b,b.b),mix(a.c,b.c),mix(a.d,b.d),mix(a.e,b.e),mix(a.f,b.f))))
    }
    companion object {
        fun freshness(frameNs: Long, imageNs: Long): Float {
            val ageMs = (imageNs-frameNs)/1_000_000f
            if (ageMs < 0f) return 0f
            return ((300f-ageMs)/150f).coerceIn(0f,1f)
        }
    }
}

object BeautyCompatibilityPolicy {
    fun rejection(preset: BeautyPreset, live: Boolean, standardPhoto: Boolean): String? = when {
        preset == BeautyPreset.OFF -> null
        live -> "自然上镜与 Live 互斥，请先关闭 Live"
        !standardPhoto -> "自然上镜仅支持普通模式，请先选择普通模式"
        else -> null
    }
    fun needsDerivative(preset: BeautyPreset, autoSave: Boolean, colorIdentity: Boolean) =
        autoSave && (preset != BeautyPreset.OFF || !colorIdentity)
}
