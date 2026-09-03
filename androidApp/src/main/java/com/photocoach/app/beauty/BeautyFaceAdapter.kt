package com.photocoach.app.beauty

import android.graphics.Matrix
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

internal fun Matrix.beautyTransform(): BeautyTransform? {
    val v = FloatArray(9); getValues(v)
    if (v[6] != 0f || v[7] != 0f || v[8] != 1f) return null
    return BeautyTransform(v[0],v[1],v[2],v[3],v[4],v[5]).takeIf { it.inverse() != null }
}

internal fun beautyFaceFrame(faces: List<Face>, timestampNs: Long,
    sensorToAnalysis: BeautyTransform): BeautyFaceFrame {
    val mask = faces.singleOrNull()?.let(::beautyMask)
    return BeautyFaceFrame(timestampNs, faces.size, sensorToAnalysis, mask)
}

private fun beautyMask(face: Face): BeautyMask? {
    fun point(type: Int) = face.getLandmark(type)?.position?.let { BeautyPoint(it.x,it.y) }
    val leftEye = point(FaceLandmark.LEFT_EYE) ?: return null
    val rightEye = point(FaceLandmark.RIGHT_EYE) ?: return null
    val nose = point(FaceLandmark.NOSE_BASE) ?: return null
    val mouth = point(FaceLandmark.MOUTH_BOTTOM) ?: return null
    val leftMouth = point(FaceLandmark.MOUTH_LEFT) ?: return null
    val rightMouth = point(FaceLandmark.MOUTH_RIGHT) ?: return null
    val leftCheek = point(FaceLandmark.LEFT_CHEEK) ?: return null
    val rightCheek = point(FaceLandmark.RIGHT_CHEEK) ?: return null
    return BeautyMaskFactory.create(face.boundingBox.width().toFloat(), face.boundingBox.height().toFloat(),
        face.headEulerAngleY, BeautyLandmarks(leftEye,rightEye,nose,mouth,leftMouth,rightMouth,leftCheek,rightCheek))
}
