import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-saludpay',
  templateUrl: './saludpay.component.html',
  styleUrls: ['./saludpay.component.css']
})
export class SaludpayComponent implements OnInit {
  // Paso 1: Login SaludPay
  cedula: string = '';
  spPassword: string = '';
  spToken: string = '';
  spError: string = '';
  loginLoading: boolean = false;

  // Paso 2: Pagos pendientes
  compraId: number = 0;
  total: number = 0;
  pagosPendientes: any[] = [];
  pagosLoading: boolean = false;

  // Paso 3: Ejecutar pago
  pagoLoading: boolean = false;
  success: boolean = false;
  pagoError: string = '';

  step: number = 1; // 1=login, 2=pagos, 3=confirmacion

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.compraId = Number(params['id'] || 0);
      this.total    = Number(params['total'] || 0);
    });
    // Pre-llenar cedula desde localStorage si esta disponible
    this.cedula = localStorage.getItem('cedula') || '';
  }

  /** Paso 1 — Autenticarse en SaludPay con cedula */
  loginSaludPay() {
    if (!this.cedula || !this.spPassword) {
      this.spError = 'Ingresa tu cedula y contraseña de SaludPay';
      return;
    }
    this.loginLoading = true;
    this.spError = '';

    this.http.post<any>('/api/saludpay/auth/login', {
      cedula: this.cedula,
      password: this.spPassword
    }).subscribe({
      next: (res) => {
        this.spToken = res.token;
        this.loginLoading = false;
        this.step = 2;
        this.cargarPagosPendientes();
      },
      error: () => {
        this.loginLoading = false;
        this.spError = 'Cedula o contraseña incorrectos en SaludPay';
      }
    });
  }

  /** Paso 2 — Cargar pagos pendientes por cedula */
  cargarPagosPendientes() {
    this.pagosLoading = true;
    const headers = new HttpHeaders({ Authorization: `Bearer ${this.spToken}` });

    this.http.get<any[]>(`/api/saludpay/pagos/mis-pagos?cedula=${this.cedula}`, { headers }).subscribe({
      next: (pagos) => {
        this.pagosPendientes = pagos.filter(p => p.estado === 'PENDIENTE_PAGO');
        this.pagosLoading = false;
      },
      error: () => {
        this.pagosLoading = false;
      }
    });
  }

  /** Paso 3 — Ejecutar el pago */
  ejecutarPago(pagoId: number) {
    this.pagoLoading = true;
    this.pagoError = '';
    const headers = new HttpHeaders({
      Authorization: `Bearer ${this.spToken}`,
      'Content-Type': 'application/json'
    });

    this.http.post<any>(`/api/saludpay/pagos/${pagoId}/pagar`, {}, { headers }).subscribe({
      next: () => {
        this.pagoLoading = false;
        this.success = true;
        this.step = 3;
        setTimeout(() => {
          this.router.navigate(['/status'], { queryParams: { id: this.compraId } });
        }, 3000);
      },
      error: () => {
        this.pagoLoading = false;
        this.pagoError = 'Error al procesar el pago. Intenta nuevamente.';
      }
    });
  }
}
