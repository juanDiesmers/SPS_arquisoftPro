using Microsoft.EntityFrameworkCore;
using SaludPay.Api.Data;
using SaludPay.Api.Dtos;
using SaludPay.Api.Models;

namespace SaludPay.Api.Services
{
    public class PagoService
    {
        private readonly SaludPayDbContext _dbContext;
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly ILogger<PagoService> _logger;

        public PagoService(SaludPayDbContext dbContext,
            IHttpClientFactory httpClientFactory,
            ILogger<PagoService> logger)
        {
            _dbContext = dbContext;
            _httpClientFactory = httpClientFactory;
            _logger = logger;
        }

        public async Task<PendingPaymentResponse> CreatePendingPaymentAsync(PendingPaymentRequest request)
        {
            var payment = new PendingPayment
            {
                CompraId = request.CompraId,
                ClienteId = request.ClienteId,
                Total = request.Total,
                Estado = request.Estado,
                Cedula = string.IsNullOrEmpty(request.Cedula) ? request.ClienteId.ToString() : request.Cedula,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            _dbContext.PendingPayments.Add(payment);
            await _dbContext.SaveChangesAsync();

            _logger.LogInformation("Creado pago pendiente en SaludPay para compra {CompraId}, Cedula={Cedula}", payment.CompraId, payment.Cedula);

            return new PendingPaymentResponse
            {
                Id = payment.Id,
                CompraId = payment.CompraId,
                ClienteId = payment.ClienteId,
                Total = payment.Total,
                Estado = payment.Estado,
                Cedula = payment.Cedula,
                CreatedAt = payment.CreatedAt,
                UpdatedAt = payment.UpdatedAt
            };
        }

        public async Task<IEnumerable<PendingPaymentResponse>> GetAllPaymentsAsync()
        {
            return await _dbContext.PendingPayments
                .AsNoTracking()
                .Select(p => new PendingPaymentResponse
                {
                    Id = p.Id,
                    CompraId = p.CompraId,
                    ClienteId = p.ClienteId,
                    Total = p.Total,
                    Estado = p.Estado,
                    Cedula = p.Cedula,
                    CreatedAt = p.CreatedAt,
                    UpdatedAt = p.UpdatedAt
                })
                .ToListAsync();
        }

        public async Task<PendingPaymentResponse?> GetPaymentByIdAsync(int id)
        {
            return await _dbContext.PendingPayments
                .AsNoTracking()
                .Where(p => p.Id == id)
                .Select(p => new PendingPaymentResponse
                {
                    Id = p.Id,
                    CompraId = p.CompraId,
                    ClienteId = p.ClienteId,
                    Total = p.Total,
                    Estado = p.Estado,
                    Cedula = p.Cedula,
                    CreatedAt = p.CreatedAt,
                    UpdatedAt = p.UpdatedAt
                })
                .FirstOrDefaultAsync();
        }

        public async Task<IEnumerable<PendingPaymentResponse>> GetPaymentsByCedulaAsync(string cedula)
        {
            return await _dbContext.PendingPayments
                .AsNoTracking()
                .Where(p => p.Cedula == cedula && p.Estado == "PENDIENTE_PAGO")
                .Select(p => new PendingPaymentResponse
                {
                    Id = p.Id,
                    CompraId = p.CompraId,
                    ClienteId = p.ClienteId,
                    Total = p.Total,
                    Estado = p.Estado,
                    Cedula = p.Cedula,
                    CreatedAt = p.CreatedAt,
                    UpdatedAt = p.UpdatedAt
                })
                .ToListAsync();
        }

        public async Task<bool> ExecutePaymentAsync(int id)
        {
            var payment = await _dbContext.PendingPayments.FindAsync(id);
            if (payment == null) return false;

            payment.Estado = "PAGADO";
            payment.UpdatedAt = DateTime.UtcNow;
            await _dbContext.SaveChangesAsync();

            await SendPaymentCallbackAsync(payment);
            return true;
        }

        public async Task<bool> ExecutePaymentByCompraIdAsync(long compraId)
        {
            var payment = await _dbContext.PendingPayments
                .FirstOrDefaultAsync(p => p.CompraId == compraId && p.Estado == "PENDIENTE_PAGO");
            if (payment == null) return false;

            payment.Estado = "PAGADO";
            payment.UpdatedAt = DateTime.UtcNow;
            await _dbContext.SaveChangesAsync();

            await SendPaymentCallbackAsync(payment);
            return true;
        }

        private async Task SendPaymentCallbackAsync(PendingPayment payment)
        {
            try
            {
                var callbackRequest = new
                {
                    compraId = payment.CompraId,
                    cedula = payment.Cedula,
                    monto = payment.Total,
                    estado = payment.Estado.Equals("RECHAZADO", StringComparison.OrdinalIgnoreCase)
                        ? "RECHAZADO"
                        : "PAGADO"
                };

                var client = _httpClientFactory.CreateClient("purchaseService");
                var response = await client.PostAsJsonAsync("/compras/webhook-pago", callbackRequest);
                response.EnsureSuccessStatusCode();

                _logger.LogInformation("Pago procesado en SaludPay y callback enviado para compra {CompraId}", payment.CompraId);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al enviar callback de pago para compra {CompraId}", payment.CompraId);
            }
        }
    }
}
