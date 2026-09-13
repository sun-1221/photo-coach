package com.photocoach.coach

enum class ResearchCondition { NONE, STATIC, DYNAMIC }
enum class ResearchScene { WINDOW, SCENERY, BACKLIGHT }

data class ResearchProtocol(val condition: ResearchCondition, val scene: ResearchScene, val configurationId: String) {
    init { require(condition == ResearchCondition.NONE || configurationId.matches(Regex("[A-Za-z0-9_-]{1,64}"))) }
    val enabled get() = condition != ResearchCondition.NONE
    val intent get() = if (scene == ResearchScene.SCENERY) ShotIntent.PERSON_WITH_SCENERY else ShotIntent.CLOSE_UP
    fun staticCues(): List<Cue>? {
        if (condition != ResearchCondition.STATIC) return null
        val text = when (scene) {
            ResearchScene.WINDOW -> "靠近窗边，让柔光照到脸" to "身体转向窗户"
            ResearchScene.SCENERY -> "人物放在左或右三分线，景物留另一侧" to "身体侧一点"
            ResearchScene.BACKLIGHT -> "点一下脸，别让脸全黑" to "下巴微收"
        }
        return listOf(Cue(CueId.MOVE_CLOSER, text.first, Audience.SHOOTER, Channel.COMPOSITION, 90),
            Cue(CueId.ANGLE_BODY, text.second, Audience.SUBJECT, Channel.POSE, 80))
    }
}
