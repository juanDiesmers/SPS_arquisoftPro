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
    }
}
