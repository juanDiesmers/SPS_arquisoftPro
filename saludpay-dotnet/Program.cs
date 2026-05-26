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
    options.UseMySql(connectionString, new MySqlServerVersion(new System.Version(8, 0, 30))));

builder.Services.AddHttpClient("purchaseService", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["SPS_SERVICE_URL"] ?? "http://purchase-service:8083");
    client.DefaultRequestHeaders.Add("Accept", "application/json");
});

builder.Services.AddHttpClient("authService", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["AUTH_SERVICE_URL"] ?? "http://auth-service:8081");
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

    // Resilient schema update: Add Cedula column to pending_payments
    try
    {
        dbContext.Database.ExecuteSqlRaw("ALTER TABLE pending_payments ADD COLUMN Cedula VARCHAR(50) NOT NULL DEFAULT '1001';");
        Console.WriteLine("Columna Cedula agregada a pending_payments.");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Nota: Columna Cedula ya existe o no pudo ser agregada: {ex.Message}");
    }

    // Resilient schema update: Create saludpay_users table
    try
    {
        dbContext.Database.ExecuteSqlRaw(
            @"CREATE TABLE IF NOT EXISTS saludpay_users (
                Id INT AUTO_INCREMENT PRIMARY KEY,
                Cedula VARCHAR(50) NOT NULL,
                Password VARCHAR(255) NOT NULL,
                UNIQUE KEY UQ_SaludPayUsers_Cedula (Cedula)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        );
        Console.WriteLine("Tabla saludpay_users asegurada.");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Error al asegurar la tabla saludpay_users: {ex.Message}");
    }

    // Seed test users
    try
    {
        var hasUsers = dbContext.Users.Any();
        if (!hasUsers)
        {
            dbContext.Users.Add(new SaludPay.Api.Models.SaludPayUser { Cedula = "1001", Password = "password123" });
            dbContext.Users.Add(new SaludPay.Api.Models.SaludPayUser { Cedula = "1002", Password = "password123" });
            dbContext.SaveChanges();
            Console.WriteLine("Usuarios de prueba sembrados en DB.");
        }
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Error al sembrar usuarios: {ex.Message}");
    }
}

app.Run();
