using System;

namespace SaludPay.Api.Models
{
    public class PendingPayment
    {
        public int Id { get; set; }
        public long CompraId { get; set; }
        public long ClienteId { get; set; }
        public decimal Total { get; set; }
        public string Estado { get; set; } = string.Empty;
        public string Cedula { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}
