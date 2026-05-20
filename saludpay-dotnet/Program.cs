using Microsoft.AspNetCore.Builder;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using SaludPay.Api.Data;
using SaludPay.Api.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var connectionString = builder.Configuration["DB_URL"] ??
    "Server=localhost;Database=saludpay_db;User=root;Password=root;";

builder.Services.AddDbContext<SaludPayDbContext>(options =>
    options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString)));

builder.Services.AddHttpClient("purchaseService", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["SPS_SERVICE_URL"] ?? "http://purchase-service:8083");
    client.DefaultRequestHeaders.Add("Accept", "application/json");
});

builder.Services.AddScoped<PagoService>();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapGet("/health", () => Results.Ok(new { status = "UP" }));
app.MapControllers();

using (var scope = app.Services.CreateScope())
{
    var dbContext = scope.ServiceProvider.GetRequiredService<SaludPayDbContext>();
    dbContext.Database.EnsureCreated();
}

app.Run();
