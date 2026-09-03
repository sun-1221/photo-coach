package com.photocoach.app.beauty

/** Explains the actual preview gate, not just the user's selected preset. No facial data leaves memory. */
enum class BeautyPreviewState(val text: String) {
    OFF("美颜已关闭"),
    WAITING_FACE("美颜等待单人正脸；当前显示原始预览"),
    UNSUPPORTED_FACE("当前人脸角度或关键点不足，美颜未应用"),
    MULTIPLE_FACES("多人画面不应用美颜"),
    STALE_FRAME("人脸分析未及时更新，美颜已暂停"),
    INVALID_TRANSFORM("美颜坐标尚未就绪，当前显示原始预览"),
    THERMAL_PAUSED("热策略已暂停美颜预览，原片和指导继续"),
    ACTIVE("实时美颜处理中（预览近似）");

    companion object {
        fun resolve(preset: BeautyPreset, enabled: Boolean, frame: BeautyFaceFrame?,
            transform: BeautyTransform?, imageTimestampNs: Long): BeautyPreviewState = when {
            preset == BeautyPreset.OFF -> OFF
            !enabled -> THERMAL_PAUSED
            frame == null || frame.faceCount == 0 -> WAITING_FACE
            frame.faceCount != 1 -> MULTIPLE_FACES
            frame.mask == null -> UNSUPPORTED_FACE
            transform?.inverse() == null -> INVALID_TRANSFORM
            BeautyFaceStore.freshness(frame.timestampNs, imageTimestampNs) <= 0f -> STALE_FRAME
            else -> ACTIVE
        }
    }
}
