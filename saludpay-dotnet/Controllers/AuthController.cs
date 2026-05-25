using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SaludPay.Api.Data;
using SaludPay.Api.Models;
using System.ComponentModel.DataAnnotations;

namespace SaludPay.Api.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly SaludPayDbContext _dbContext;

        public AuthController(SaludPayDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var user = await _dbContext.Users
                .FirstOrDefaultAsync(u => u.Cedula == request.Cedula);

            if (user == null)
            {
                // Register user dynamically on-the-fly!
                user = new SaludPayUser
                {
                    Cedula = request.Cedula,
                    Password = request.Password
                };
                _dbContext.Users.Add(user);
                await _dbContext.SaveChangesAsync();
            }
            else if (user.Password != request.Password)
            {
                return Unauthorized(new { message = "Cédula o contraseña incorrectas." });
            }

            return Ok(new 
            { 
                success = true,
                message = "Autenticación exitosa.", 
                cedula = user.Cedula 
            });
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
