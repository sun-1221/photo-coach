package com.photocoach.coach

/** A current, unnumbered status hint; never an extra required step or a shutter gate. */
enum class ReadinessIssue(val text: String) {
    NO_RECENT_SIGNAL("暂未确认画面，快门仍可使用"),
    NO_FACE("请露出脸，或靠近一点"),
    MULTIPLE_PEOPLE("多人画面暂不判断就绪，快门仍可使用"),
    LENS_OBSCURED("镜头可能被挡住，请检查"),
    SUBJECT_CUT_OFF("人物贴边，请把主体留全"),
    JOINTS_NEAR_EDGE("手肘和膝盖别贴边"),
    FACE_TOO_LOW("把脸放到上方三分线"),
    FACE_TURNED_AWAY("脸转回镜头一点"),
    EYES_CLOSED("眼睛睁开一点，看镜头"),
    FOCUS_OFF_FACE("点一下脸，重新对焦"),
    FACE_DARK("脸部偏暗，可点「调亮」增加曝光"),
    SEVERE_EXPOSURE("高光过亮，请调低曝光"),
    SUBJECT_TOO_SMALL("人物偏小，请调整拍摄距离"),
    PHONE_TILTED("沿网格放平手机"),
    PHONE_MOVING("稳住手机，稍停一下"),
    SUBJECT_MOVING("等人物动作停一下再拍"),
}
