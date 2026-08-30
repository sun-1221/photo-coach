namespace PhotoCoach.ExplainApi;

public sealed record ExplainTip(string Text, string Audience, string Channel);

public sealed record ExplainSignals(
    int FaceCount,
    float FaceRatio,
    bool FaceDarkerThanScene,
    float TiltDegrees,
    string CoarseScene,
    bool PoseAvailable,
    string SuggestedMode);

public sealed record ExplainRequest(
    string ImageJpegBase64,
    ExplainSignals? Signals,
    IReadOnlyList<ExplainTip>? CurrentTips);

public sealed record ExplainResponse(
    IReadOnlyList<ExplainTip> Tips,
    string Reason,
    bool Available);
