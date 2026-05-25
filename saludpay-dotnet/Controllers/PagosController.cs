using Microsoft.AspNetCore.Mvc;
using SaludPay.Api.Dtos;
using SaludPay.Api.Services;

namespace SaludPay.Api.Controllers
{
    [ApiController]
    [Route("api/pagos")]
    public class PagosController : ControllerBase
    {
        private readonly PagoService _pagoService;

        public PagosController(PagoService pagoService)
        {
            _pagoService = pagoService;
        }

        [HttpPost("pendientes")]
        public async Task<IActionResult> CreatePendingPayment([FromBody] PendingPaymentRequest request)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            var payment = await _pagoService.CreatePendingPaymentAsync(request);
            return CreatedAtAction(nameof(GetPaymentById), new { id = payment.Id }, payment);
        }

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var payments = await _pagoService.GetAllPaymentsAsync();
            return Ok(payments);
        }

        [HttpGet("{id:int}")]
        public async Task<IActionResult> GetPaymentById(int id)
        {
            var payment = await _pagoService.GetPaymentByIdAsync(id);
            if (payment == null)
            {
                return NotFound();
            }
            return Ok(payment);
        }

        [HttpGet("mis-pagos")]
        public async Task<IActionResult> GetPaymentsByCedula([FromQuery] string cedula)
        {
            if (string.IsNullOrEmpty(cedula))
            {
                return BadRequest("La cédula es requerida.");
            }
            var payments = await _pagoService.GetPaymentsByCedulaAsync(cedula);
            return Ok(payments);
        }

        [HttpPost("{id:int}/pagar")]
        public async Task<IActionResult> ExecutePayment(int id)
        {
            var success = await _pagoService.ExecutePaymentAsync(id);
            if (!success)
            {
                return NotFound("Pago pendiente no encontrado.");
            }
            return Ok(new { message = "Pago realizado exitosamente." });
        }

        [HttpPost("/api/pagar")]
        public async Task<IActionResult> ExecuteLegacyPayment([FromBody] LegacyPagarRequest request)
        {
            var success = await _pagoService.ExecutePaymentByCompraIdAsync(request.CompraId);
            if (!success)
            {
                return NotFound("Pago pendiente no encontrado o ya procesado para esta compra.");
            }
            return Ok(new { message = "Pago realizado exitosamente (Legacy)." });
        }
    }

    public class LegacyPagarRequest
    {
        public long CompraId { get; set; }
    }
}
