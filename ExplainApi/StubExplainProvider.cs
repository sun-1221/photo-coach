namespace PhotoCoach.ExplainApi;

public sealed class StubExplainProvider : IExplainProvider
{
    public Task<ExplainResponse> ExplainAsync(ExplainRequest request, CancellationToken cancellationToken)
    {
        cancellationToken.ThrowIfCancellationRequested();
        var current = request.CurrentTips ?? Array.Empty<ExplainTip>();
        var tips = current
            .Where(tip => IsAudienceValid(tip.Audience))
            .Take(3)
            .Select(tip => tip with { Text = tip.Text })
            .ToArray();
        if (tips.Length == 0)
        {
            tips =
            [
                new ExplainTip("点主体，主体别居中挤死", "shooter", "composition"),
            ];
        }

        return Task.FromResult(new ExplainResponse(
            Tips: tips,
            Reason: "补充当前三条，没有另起一套话。",
            Available: true));
    }

    private static bool IsAudienceValid(string audience) =>
        audience is "shooter" or "subject" or "proxy";
}
