using System.Net.Http.Json;

namespace PhotoCoach.ExplainApi;

public sealed class HttpExplainProvider(HttpClient httpClient, string endpoint) : IExplainProvider
{
    public async Task<ExplainResponse> ExplainAsync(ExplainRequest request, CancellationToken cancellationToken)
    {
        using var response = await httpClient.PostAsJsonAsync(endpoint, request, cancellationToken).ConfigureAwait(false);
        response.EnsureSuccessStatusCode();
        var body = await response.Content.ReadFromJsonAsync<ExplainResponse>(cancellationToken).ConfigureAwait(false);
        return body ?? new ExplainResponse([], "讲解暂时不可用", false);
    }
}
