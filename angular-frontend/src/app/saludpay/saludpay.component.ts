import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-saludpay',
  templateUrl: './saludpay.component.html',
  styleUrls: ['./saludpay.component.css']
})
export class SaludpayComponent implements OnInit {
  compraId: number = 0;
  total: number = 0;
  loading = false;
  success = false;

  constructor(private route: ActivatedRoute, private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.compraId = Number(params['id']);
      this.total = Number(params['total']);
    });
  }

  pagar() {
    this.loading = true;
    const body = {
      compraId: this.compraId,
      clienteId: Number(localStorage.getItem('userId')),
      monto: this.total,
      metodoPago: 'TARJETA_CREDITO'
    };
    // The gateway routes /api/saludpay to SaludPay .NET Service
    this.http.post('/api/saludpay/pagar', body).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        setTimeout(() => {
          this.router.navigate(['/status'], { queryParams: { id: this.compraId } });
        }, 3000);
      },
      error: () => {
        this.loading = false;
        alert('Error en el pago');
      }
    });
  }
}
