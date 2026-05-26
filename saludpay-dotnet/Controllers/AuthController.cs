using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SaludPay.Api.Data;
using SaludPay.Api.Models;
using System.ComponentModel.DataAnnotations;
using System.Net.Http.Json;

namespace SaludPay.Api.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly SaludPayDbContext _dbContext;
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly ILogger<AuthController> _logger;

        public AuthController(SaludPayDbContext dbContext, IHttpClientFactory httpClientFactory, ILogger<AuthController> logger)
        {
            _dbContext = dbContext;
            _httpClientFactory = httpClientFactory;
            _logger = logger;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            try
            {
                var client = _httpClientFactory.CreateClient("authService");
                var authRequest = new { cedula = request.Cedula, password = request.Password };
                var response = await client.PostAsJsonAsync("/auth/validate-cedula", authRequest);

                if (!response.IsSuccessStatusCode)
                {
                    _logger.LogWarning("Validacion de credenciales fallida en auth-service para cedula={Cedula}", request.Cedula);
                    return Unauthorized(new { message = "Cedula o contrasena incorrectas." });
                }

                // If auth-service validates the credentials, synchronize/ensure the user exists locally in saludpay_db
                var user = await _dbContext.Users.FirstOrDefaultAsync(u => u.Cedula == request.Cedula);
                if (user == null)
                {
                    user = new SaludPayUser
                    {
                        Cedula = request.Cedula,
                        Password = request.Password
                    };
                    _dbContext.Users.Add(user);
                    await _dbContext.SaveChangesAsync();
                    _logger.LogInformation("Usuario {Cedula} registrado localmente en SaludPay despues de validar con SPS.", request.Cedula);
                }
                else if (user.Password != request.Password)
                {
                    // Update password locally to match SPS
                    user.Password = request.Password;
                    await _dbContext.SaveChangesAsync();
                    _logger.LogInformation("Contrasena de usuario {Cedula} actualizada localmente en SaludPay.", request.Cedula);
                }

                return Ok(new 
                { 
                    success = true,
                    message = "Autenticacion exitosa.", 
                    cedula = user.Cedula,
                    token = "dummy-token-for-saludpay"
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al comunicarse con auth-service para validar cedula={Cedula}", request.Cedula);
                // Fallback to local DB check in case auth-service is down (graceful degradation)
                var localUser = await _dbContext.Users.FirstOrDefaultAsync(u => u.Cedula == request.Cedula);
                if (localUser != null && localUser.Password == request.Password)
                {
                    _logger.LogInformation("Autenticacion local fallback exitosa para cedula={Cedula}", request.Cedula);
                    return Ok(new 
                    { 
                        success = true,
                        message = "Autenticacion exitosa (Local Fallback).", 
                        cedula = localUser.Cedula,
                        token = "dummy-token-for-saludpay"
                    });
                }
                return StatusCode(500, new { message = "Error de conexion con el servicio de autenticacion.", detail = ex.Message });
            }
        }
    }

    public class LoginRequest
    {
        [Required]
        public string Cedula { get; set; } = string.Empty;

        [Required]
        public string Password { get; set; } = string.Empty;
    }
}
