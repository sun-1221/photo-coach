package com.photocoach.app.beauty

import kotlin.math.abs
import kotlin.math.hypot

internal data class BeautyLandmarks(
    val leftEye: BeautyPoint, val rightEye: BeautyPoint, val nose: BeautyPoint,
    val mouth: BeautyPoint, val leftMouth: BeautyPoint, val rightMouth: BeautyPoint,
    val leftCheek: BeautyPoint, val rightCheek: BeautyPoint,
)

/** Geometry gate only; ML Kit landmarks do not prove that skin is unoccluded. */
internal object BeautyMaskFactory {
    fun create(width: Float, height: Float, yaw: Float, landmarks: BeautyLandmarks?): BeautyMask? {
        if (landmarks == null || !yaw.isFinite() || abs(yaw) > 35f ||
            !width.isFinite() || !height.isFinite() || width < 24f || height < 24f) return null
        val points = with(landmarks) { listOf(leftEye, rightEye, nose, mouth, leftMouth, rightMouth, leftCheek, rightCheek) }
        if (points.any { !it.x.isFinite() || !it.y.isFinite() }) return null
        val eyes = listOf(landmarks.leftEye, landmarks.rightEye).sortedBy { it.x }
        val distance = hypot(eyes[1].x-eyes[0].x, eyes[1].y-eyes[0].y)
        if (distance < width*.18f || distance > width*.8f) return null
        val ux = (eyes[1].x-eyes[0].x)/distance
        val uy = (eyes[1].y-eyes[0].y)/distance
        val cx = (eyes[0].x+eyes[1].x)/2
        val cy = (eyes[0].y+eyes[1].y)/2
        val transform = BeautyTransform(ux/width, uy/width, .5f-(ux*cx+uy*cy)/width,
            -uy/height, ux/height, .4f-(-uy*cx+ux*cy)/height)
        val nose = transform.map(landmarks.nose)
        val mouth = transform.map(landmarks.mouth)
        if (nose.y !in .42f.. .78f || mouth.y !in .55f.. .95f || mouth.y <= nose.y) return null
        fun ellipse(p: BeautyPoint, rx: Float, ry: Float, dy: Float = 0f): BeautyEllipse {
            val q = transform.map(p)
            return BeautyEllipse(q.x, q.y+dy, rx, ry)
        }
        val mouthWidth = hypot(landmarks.leftMouth.x-landmarks.rightMouth.x,
            landmarks.leftMouth.y-landmarks.rightMouth.y)/width
        return BeautyMask(transform,
            listOf(ellipse(landmarks.leftCheek,.18f,.21f), ellipse(landmarks.rightCheek,.18f,.21f),
                BeautyEllipse(.5f,.22f,.27f,.12f), ellipse(landmarks.mouth,.18f,.09f,.13f)),
            listOf(ellipse(landmarks.leftEye,.16f,.12f,-.025f), ellipse(landmarks.rightEye,.16f,.12f,-.025f),
                ellipse(landmarks.nose,.15f,.13f), ellipse(landmarks.mouth,maxOf(.26f,mouthWidth*.7f),.12f,-.02f)))
    }
}
