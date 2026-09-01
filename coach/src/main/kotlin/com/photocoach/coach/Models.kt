package com.photocoach.coach

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Audience {
    @SerialName("shooter")
    SHOOTER,

    @SerialName("subject")
    SUBJECT,

    @SerialName("proxy")
    PROXY,
}

@Serializable
enum class Channel {
    @SerialName("composition")
    COMPOSITION,

    @SerialName("light")
    LIGHT,

    @SerialName("pose")
    POSE,
}

@Serializable
enum class ShotIntent {
    @SerialName("close_up")
    CLOSE_UP,

    @SerialName("person_with_scenery")
    PERSON_WITH_SCENERY,
}

@Serializable
enum class SceneId {
    @SerialName("window_portrait")
    WINDOW_PORTRAIT,

    @SerialName("outdoor_with_scenery")
    OUTDOOR_WITH_SCENERY,

    @SerialName("backlit_portrait")
    BACKLIT_PORTRAIT,
}

@Serializable
enum class CoarseScene {
    @SerialName("portrait")
    PORTRAIT,

    @SerialName("outdoor")
    OUTDOOR,

    @SerialName("indoor")
    INDOOR,

    @SerialName("unknown")
    UNKNOWN,
}

@Serializable
enum class SuggestedMode {
    @SerialName("PHOTO")
    PHOTO,

    @SerialName("PORTRAIT")
    PORTRAIT,

    @SerialName("HDR")
    HDR,

    @SerialName("NIGHT")
    NIGHT,
}

@Serializable
enum class FocusTarget {
    @SerialName("FACE")
    FACE,

    @SerialName("PERSON")
    PERSON,
}

@Serializable
enum class CueId {
    @SerialName("find_person")
    FIND_PERSON,

    @SerialName("clean_lens")
    CLEAN_LENS,

    @SerialName("keep_subject_in_frame")
    KEEP_SUBJECT_IN_FRAME,

    @SerialName("focus_face")
    FOCUS_FACE,

    @SerialName("move_closer")
    MOVE_CLOSER,

    @SerialName("move_closer_keep_scenery")
    MOVE_CLOSER_KEEP_SCENERY,

    @SerialName("level_phone")
    LEVEL_PHONE,

    @SerialName("place_on_thirds")
    PLACE_ON_THIRDS,

    @SerialName("place_face_on_upper_third")
    PLACE_FACE_ON_UPPER_THIRD,

    @SerialName("lower_exposure")
    LOWER_EXPOSURE,

    @SerialName("turn_face_to_camera")
    TURN_FACE_TO_CAMERA,

    @SerialName("open_eyes")
    OPEN_EYES,

    @SerialName("chin_down")
    CHIN_DOWN,

    @SerialName("angle_body")
    ANGLE_BODY,

    @SerialName("relax_shoulders")
    RELAX_SHOULDERS,

    @SerialName("rest_hands")
    REST_HANDS,

    @SerialName("move_to_window")
    MOVE_TO_WINDOW,

    @SerialName("turn_to_window")
    TURN_TO_WINDOW,

    @SerialName("look_at_landmark")
    LOOK_AT_LANDMARK,

    @SerialName("step_sideways")
    STEP_SIDEWAYS,

    @SerialName("angle_body_backlit")
    ANGLE_BODY_BACKLIT,

    @SerialName("p1_technique")
    P1_TECHNIQUE,
}

@Serializable
data class Cue(
    val id: CueId,
    val text: String,
    val audience: Audience,
    val channel: Channel,
    val priority: Int = 0,
    val directionGroup: String? = null,
    val direction: Int = 0,
    val critical: Boolean = false,
)

@Serializable
data class SceneStartParams(
    val mode: SuggestedMode,
    val preferTelephoto: Boolean,
    val focus: FocusTarget,
    val evBias: Float? = null,
    val flashOff: Boolean = true,
)

data class Signals(
    val faceCount: Int = 0,
    val faceRatio: Float = 0f,
    val faceDarkerThanScene: Boolean = false,
    val tiltDegrees: Float = 0f,
    val coarseScene: CoarseScene = CoarseScene.UNKNOWN,
    val poseAvailable: Boolean = false,
    val hasTelephotoPreset: Boolean = false,
    val oneSideBrighter: Boolean = false,
    val hasLargeEnvironment: Boolean = false,
    val personCentered: Boolean = false,
    val faceTooLowInFrame: Boolean = false,
    val focusOnFace: Boolean = true,
    val faceTurnedAway: Boolean = false,
    val eyesLikelyClosed: Boolean = false,
    val headTiltedBack: Boolean = false,
    val shouldersSquare: Boolean = false,
    val shouldersRaised: Boolean = false,
    val handsIdle: Boolean = false,
    val skyOverexposed: Boolean = false,
    val subjectCutOff: Boolean = false,
    val lensObscured: Boolean = false,
    val faceReliable: Boolean = false,
    val poseReliable: Boolean = false,
    val anklesVisible: Boolean = false,
    val anklesNearBottomEdge: Boolean = false,
    val jointsNearFrameEdge: Boolean = false,
    val atLeastOneHandOutsideTorso: Boolean = false,
    val seatedCandidate: Boolean = false,
    val torsoUpright: Boolean = false,
    val walkingCandidate: Boolean = false,
    val walkingMotionStable: Boolean = false,
    val subjectMotionHigh: Boolean = false,
    val backgroundEdgeDensityHigh: Boolean = false,
    val handheldStable: Boolean = true,
    val meanLuma: Float? = null,
    val highlightRatio: Float? = null,
)

data class OverlayHint(
    val showSilhouette: Boolean,
    val showThirds: Boolean = true,
    val showHorizon: Boolean = true,
)

data class CoachOutput(
    val intent: ShotIntent,
    val sceneId: SceneId?,
    val cues: List<Cue>,
    val startParams: SceneStartParams,
    val overlay: OverlayHint,
)
