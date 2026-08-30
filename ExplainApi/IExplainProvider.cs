namespace PhotoCoach.ExplainApi;

public interface IExplainProvider
{
    Task<ExplainResponse> ExplainAsync(ExplainRequest request, CancellationToken cancellationToken);
}
