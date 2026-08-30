using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using PhotoCoach.ExplainApi;
using Xunit;

namespace PhotoCoach.ExplainApi.Tests;

public sealed class ExplainApiTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public ExplainApiTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory;
    }

    [Fact]
    public async Task Explain_supplements_current_tips()
    {
        var client = _factory.CreateClient();
        var request = SampleRequest(
        [
            new ExplainTip("走近两步", "shooter", "composition"),
            new ExplainTip("点脸，锁住对焦", "shooter", "light"),
            new ExplainTip("下巴微收，头略往前", "subject", "pose"),
        ]);

        var response = await client.PostAsJsonAsync("/v1/explain", request);
        response.EnsureSuccessStatusCode();
        var body = await response.Content.ReadFromJsonAsync<ExplainResponse>();

        Assert.NotNull(body);
        Assert.True(body.Available);
        Assert.InRange(body.Tips.Count, 1, 3);
        Assert.All(body.Tips, tip => Assert.Contains(tip.Audience, new[] { "shooter", "subject", "proxy" }));
        Assert.False(string.IsNullOrWhiteSpace(body.Reason));
    }

    [Fact]
    public async Task Provider_failure_returns_unavailable_without_throwing()
    {
        var factory = _factory.WithWebHostBuilder(builder =>
        {
            builder.ConfigureServices(services =>
            {
                services.AddSingleton<IExplainProvider, FailingExplainProvider>();
            });
        });
        var client = factory.CreateClient();

        var response = await client.PostAsJsonAsync("/v1/explain", SampleRequest([]));
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var body = await response.Content.ReadFromJsonAsync<ExplainResponse>();
        Assert.NotNull(body);
        Assert.False(body.Available);
        Assert.Equal("讲解暂时不可用", body.Reason);
    }

    [Fact]
    public async Task Drops_invalid_audience()
    {
        var client = _factory.CreateClient();
        var request = SampleRequest(
        [
            new ExplainTip("走近两步", "nobody", "composition"),
            new ExplainTip("放平手机", "shooter", "composition"),
        ]);

        var body = await (await client.PostAsJsonAsync("/v1/explain", request))
            .Content.ReadFromJsonAsync<ExplainResponse>();

        Assert.NotNull(body);
        Assert.DoesNotContain(body.Tips, tip => tip.Audience == "nobody");
    }

    private static ExplainRequest SampleRequest(IReadOnlyList<ExplainTip> tips) =>
        new(
            ImageJpegBase64: "dGVzdA==",
            Signals: new ExplainSignals(1, 0.16f, false, 1.2f, "indoor", true, "PHOTO"),
            CurrentTips: tips);

    private sealed class FailingExplainProvider : IExplainProvider
    {
        public Task<ExplainResponse> ExplainAsync(ExplainRequest request, CancellationToken cancellationToken) =>
            throw new InvalidOperationException("provider down");
    }
}
