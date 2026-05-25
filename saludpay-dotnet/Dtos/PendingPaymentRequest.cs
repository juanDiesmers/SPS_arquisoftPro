using System.ComponentModel.DataAnnotations;

namespace SaludPay.Api.Dtos
{
    public class PendingPaymentRequest
    {
        [Required]
        public long CompraId { get; set; }

        [Required]
        public long ClienteId { get; set; }

        [Required]
        public decimal Total { get; set; }

        [Required]
        public string Estado { get; set; } = string.Empty;

        public string Cedula { get; set; } = string.Empty;
    }
}
