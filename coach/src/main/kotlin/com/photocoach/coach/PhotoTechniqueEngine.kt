package com.photocoach.coach

enum class TechniqueCategory { COMPOSITION, JOINT_CROP, LIGHT, FOCAL_DISTANCE, BACKGROUND, NIGHT, MOTION, MULTI_PERSON_SAFE }
data class TechniqueCapabilities(val calibratedTelephotoLabel: String? = null, val burstEnabled: Boolean = false)
data class TechniqueSuggestion(val category: TechniqueCategory, val text: String, val reason: String, val audience: Audience)

object PhotoTechniqueEngine {
    fun suggest(signals: Signals, capabilities: TechniqueCapabilities): TechniqueSuggestion? {
        if (signals.faceCount > 1) return multiPersonSuggestion(signals)
        return when {
            signals.subjectCutOff -> suggestion(TechniqueCategory.COMPOSITION, "人物往画面里站一点", "人物贴近画面边缘")
            signals.jointsNearFrameEdge -> suggestion(TechniqueCategory.JOINT_CROP, "关节别贴着画面边缘", "可靠关节接近裁切边缘")
            signals.faceDarkerThanScene -> suggestion(TechniqueCategory.LIGHT, "让人物转向亮的一边", "脸部比背景明显更暗")
            signals.skyOverexposed -> suggestion(TechniqueCategory.LIGHT, "曝光降一点", "画面高光明显溢出")
            signals.faceRatio in 0.001f..<0.05f && capabilities.calibratedTelephotoLabel != null ->
                suggestion(TechniqueCategory.FOCAL_DISTANCE, "退后一点，再切${capabilities.calibratedTelephotoLabel}", "人物较小且焦段已验收")
            signals.backgroundEdgeDensityHigh -> suggestion(TechniqueCategory.BACKGROUND, "换个更干净的背景", "人物附近边缘较密集")
            signals.meanLuma?.let { it < 55f } == true && !signals.handheldStable ->
                suggestion(TechniqueCategory.NIGHT, "扶稳手机，再拍一张", "暗光下手持不稳定")
            signals.subjectMotionHigh -> suggestion(TechniqueCategory.MOTION,
                if (capabilities.burstEnabled) "保持三张连拍，等动作停一下" else "等动作停一下再拍", "人物运动较快")
            else -> null
        }
    }

    private fun multiPersonSuggestion(signals: Signals): TechniqueSuggestion? = when {
        signals.subjectCutOff -> suggestion(TechniqueCategory.MULTI_PERSON_SAFE, "人物再往画面里靠一点", "多人画面有人贴边")
        kotlin.math.abs(signals.tiltDegrees) > 3f -> suggestion(TechniqueCategory.MULTI_PERSON_SAFE, "沿网格放平手机", "多人画面倾斜")
        signals.skyOverexposed -> suggestion(TechniqueCategory.MULTI_PERSON_SAFE, "曝光降一点", "多人画面高光溢出")
        else -> null
    }

    private fun suggestion(category: TechniqueCategory, text: String, reason: String) =
        TechniqueSuggestion(category, text, reason, Audience.SHOOTER)
}
