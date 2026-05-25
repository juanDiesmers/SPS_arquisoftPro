using System;

namespace SaludPay.Api.Models
{
    public class SaludPayUser
    {
        public int Id { get; set; }
        public string Cedula { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }
}
