using PhotoCoach.ExplainApi;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddHttpClient();
builder.Services.AddSingleton<IExplainProvider>(sp =>
{
    var endpoint = builder.Configuration["Explain:ProviderUrl"];
    if (string.IsNullOrWhiteSpace(endpoint))
    {
        return new StubExplainProvider();
    }

    return new HttpExplainProvider(sp.GetRequiredService<IHttpClientFactory>().CreateClient(), endpoint);
});

var app = builder.Build();

app.MapPost("/v1/explain", async (ExplainRequest request, IExplainProvider provider, CancellationToken cancellationToken) =>
{
    try
    {
        var result = await provider.ExplainAsync(request, cancellationToken).ConfigureAwait(false);
        var tips = result.Tips
            .Where(tip => tip.Audience is "shooter" or "subject" or "proxy")
            .Take(3)
            .ToArray();
        return Results.Ok(result with { Tips = tips });
    }
    catch (Exception)
    {
        return Results.Ok(new ExplainResponse([], "讲解暂时不可用", false));
    }
});

app.Run();

public partial class Program;
