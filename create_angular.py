import os

base_dir = r"c:\Users\juand\SPS_arquisoftPro\angular-frontend\src\app"

files = {
    "app.module.ts": """import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { CatalogComponent } from './catalog/catalog.component';
import { CartComponent } from './cart/cart.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { StatusComponent } from './status/status.component';
import { SaludpayComponent } from './saludpay/saludpay.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    CatalogComponent,
    CartComponent,
    CheckoutComponent,
    StatusComponent,
    SaludpayComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
""",

    "app-routing.module.ts": """import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { CatalogComponent } from './catalog/catalog.component';
import { CartComponent } from './cart/cart.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { StatusComponent } from './status/status.component';
import { SaludpayComponent } from './saludpay/saludpay.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'catalog', component: CatalogComponent },
  { path: 'cart', component: CartComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'status', component: StatusComponent },
  { path: 'saludpay', component: SaludpayComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
""",

    "app.component.html": """<div class="app-container">
  <nav class="navbar" *ngIf="isLoggedIn()">
    <div class="navbar-brand">SPS - Salud Pay System</div>
    <div class="navbar-menu">
      <a routerLink="/catalog">Catálogo</a>
      <a routerLink="/cart">Carrito</a>
      <a routerLink="/status">Mis Compras</a>
      <a (click)="logout()">Salir</a>
    </div>
  </nav>
  <main class="main-content">
    <router-outlet></router-outlet>
  </main>
</div>
""",

    "app.component.ts": """import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'SPS Frontend';

  constructor(private router: Router) {}

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    this.router.navigate(['/login']);
  }
}
""",

    "app.component.css": """
.app-container { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.navbar { background-color: #0f4c75; color: white; display: flex; justify-content: space-between; padding: 1rem 2rem; align-items: center; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
.navbar-brand { font-size: 1.5rem; font-weight: bold; }
.navbar-menu a { color: white; text-decoration: none; margin-left: 1.5rem; cursor: pointer; font-weight: 500; }
.navbar-menu a:hover { color: #bbe1fa; }
.main-content { padding: 2rem; max-width: 1200px; margin: 0 auto; }
""",

    "services/auth.service.ts": """import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth';
  constructor(private http: HttpClient) { }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }
}
""",

    "services/catalog.service.ts": """import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private apiUrl = '/api/catalog/planes';
  constructor(private http: HttpClient) { }

  getHeaders() {
    return new HttpHeaders().set('Authorization', `Bearer ${localStorage.getItem('token')}`);
  }

  getPlanes(): Observable<any> {
    return this.http.get(this.apiUrl, { headers: this.getHeaders() });
  }
}
""",

    "services/purchase.service.ts": """import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PurchaseService {
  private apiUrl = '/api/purchase/compras';
  constructor(private http: HttpClient) { }

  getHeaders() {
    return new HttpHeaders().set('Authorization', `Bearer ${localStorage.getItem('token')}`);
  }

  createPurchase(data: any): Observable<any> {
    return this.http.post(this.apiUrl, data, { headers: this.getHeaders() });
  }

  getPurchase(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}
""",

    "login/login.component.ts": """import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.authService.login(this.credentials).subscribe({
      next: (res) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('userId', res.userId);
        this.router.navigate(['/catalog']);
      },
      error: (err) => this.error = 'Credenciales inválidas'
    });
  }
}
""",

    "login/login.component.html": """<div class="login-container">
  <div class="login-card">
    <h2>Ingreso a SPS</h2>
    <div *ngIf="error" class="error">{{ error }}</div>
    <div class="form-group">
      <label>Usuario</label>
      <input type="text" [(ngModel)]="credentials.username" class="form-control" placeholder="admin">
    </div>
    <div class="form-group">
      <label>Contraseña</label>
      <input type="password" [(ngModel)]="credentials.password" class="form-control" placeholder="admin">
    </div>
    <button (click)="login()" class="btn btn-primary">Ingresar</button>
  </div>
</div>
""",

    "login/login.component.css": """
.login-container { display: flex; justify-content: center; align-items: center; height: 80vh; background-color: #f4f7f6; }
.login-card { background: white; padding: 2.5rem; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); width: 350px; }
h2 { text-align: center; color: #3282b8; margin-bottom: 1.5rem; }
.form-group { margin-bottom: 1rem; }
label { display: block; margin-bottom: 0.5rem; font-weight: 600; color: #1b262c; }
.form-control { width: 100%; padding: 0.75rem; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; }
.btn { width: 100%; padding: 0.75rem; border: none; border-radius: 6px; font-size: 1rem; cursor: pointer; font-weight: bold; margin-top: 1rem; }
.btn-primary { background-color: #3282b8; color: white; transition: background-color 0.3s; }
.btn-primary:hover { background-color: #0f4c75; }
.error { color: #d9534f; background: #f2dede; padding: 0.5rem; border-radius: 4px; margin-bottom: 1rem; text-align: center; font-size: 0.9rem; }
""",

    "catalog/catalog.component.ts": """import { Component, OnInit } from '@angular/core';
import { CatalogService } from '../services/catalog.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent implements OnInit {
  planes: any[] = [];

  constructor(private catalogService: CatalogService, private router: Router) {}

  ngOnInit(): void {
    this.catalogService.getPlanes().subscribe(res => {
      this.planes = res;
    });
  }

  addToCart(plan: any) {
    let cart = JSON.parse(localStorage.getItem('cart') || '[]');
    if (!cart.some((p: any) => p.id === plan.id)) {
      cart.push(plan);
      localStorage.setItem('cart', JSON.stringify(cart));
      alert('Plan agregado al carrito');
    } else {
      alert('El plan ya está en el carrito');
    }
  }
}
""",

    "catalog/catalog.component.html": """<div class="catalog-header">
  <h2>Catálogo de Planes de Salud</h2>
</div>
<div class="planes-grid">
  <div class="plan-card" *ngFor="let plan of planes">
    <div class="plan-title">{{ plan.nombre }}</div>
    <div class="plan-price">${{ plan.precio }}</div>
    <p class="plan-desc">{{ plan.descripcion }}</p>
    <button (click)="addToCart(plan)" class="btn btn-outline">Agregar al Carrito</button>
  </div>
</div>
""",

    "catalog/catalog.component.css": """
.catalog-header { margin-bottom: 2rem; color: #1b262c; }
.planes-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 2rem; }
.plan-card { background: white; border-radius: 12px; padding: 1.5rem; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border-top: 4px solid #3282b8; transition: transform 0.2s; }
.plan-card:hover { transform: translateY(-5px); }
.plan-title { font-size: 1.25rem; font-weight: bold; color: #0f4c75; margin-bottom: 0.5rem; }
.plan-price { font-size: 1.5rem; font-weight: 900; color: #1b262c; margin-bottom: 1rem; }
.plan-desc { color: #666; font-size: 0.9rem; margin-bottom: 1.5rem; line-height: 1.5; }
.btn-outline { width: 100%; background: transparent; border: 2px solid #3282b8; color: #3282b8; padding: 0.75rem; border-radius: 6px; font-weight: bold; cursor: pointer; transition: all 0.3s; }
.btn-outline:hover { background: #3282b8; color: white; }
""",

    "cart/cart.component.ts": """import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  cart: any[] = [];
  total: number = 0;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.cart = JSON.parse(localStorage.getItem('cart') || '[]');
    this.calculateTotal();
  }

  calculateTotal() {
    this.total = this.cart.reduce((sum, p) => sum + p.precio, 0);
  }

  remove(index: number) {
    this.cart.splice(index, 1);
    localStorage.setItem('cart', JSON.stringify(this.cart));
    this.calculateTotal();
  }

  checkout() {
    if (this.cart.length > 0) {
      this.router.navigate(['/checkout']);
    }
  }
}
""",

    "cart/cart.component.html": """<div class="cart-container">
  <h2>Tu Carrito de Compras</h2>
  
  <div *ngIf="cart.length === 0" class="empty-cart">
    Tu carrito está vacío. <a routerLink="/catalog">Ir al catálogo</a>
  </div>

  <div *ngIf="cart.length > 0">
    <table class="cart-table">
      <thead>
        <tr>
          <th>Plan</th>
          <th>Precio</th>
          <th>Acción</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let plan of cart; let i = index">
          <td>{{ plan.nombre }}</td>
          <td class="price">${{ plan.precio }}</td>
          <td><button (click)="remove(i)" class="btn-remove">X</button></td>
        </tr>
      </tbody>
      <tfoot>
        <tr>
          <td><strong>Total</strong></td>
          <td class="price"><strong>${{ total }}</strong></td>
          <td></td>
        </tr>
      </tfoot>
    </table>
    <div class="cart-actions">
      <button (click)="checkout()" class="btn btn-primary">Proceder al Checkout</button>
    </div>
  </div>
</div>
""",

    "cart/cart.component.css": """
.cart-container { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.empty-cart { text-align: center; padding: 3rem; color: #666; font-size: 1.1rem; }
.cart-table { width: 100%; border-collapse: collapse; margin-top: 1.5rem; }
.cart-table th, .cart-table td { padding: 1rem; text-align: left; border-bottom: 1px solid #eee; }
.cart-table th { color: #0f4c75; font-weight: 600; background-color: #f8f9fa; }
.price { font-weight: bold; color: #1b262c; }
.btn-remove { background: #d9534f; color: white; border: none; padding: 0.4rem 0.8rem; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-remove:hover { background: #c9302c; }
.cart-actions { margin-top: 2rem; text-align: right; }
.btn-primary { padding: 0.75rem 2rem; background-color: #3282b8; color: white; border: none; border-radius: 6px; font-size: 1.1rem; font-weight: bold; cursor: pointer; }
.btn-primary:hover { background-color: #0f4c75; }
""",

    "checkout/checkout.component.ts": """import { Component, OnInit } from '@angular/core';
import { PurchaseService } from '../services/purchase.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  cart: any[] = [];
  total: number = 0;
  loading: boolean = false;

  constructor(private purchaseService: PurchaseService, private router: Router) {}

  ngOnInit(): void {
    this.cart = JSON.parse(localStorage.getItem('cart') || '[]');
    this.total = this.cart.reduce((sum, p) => sum + p.precio, 0);
  }

  confirmPurchase() {
    this.loading = true;
    const planIds = this.cart.map(p => p.id);
    const userId = Number(localStorage.getItem('userId'));

    const request = {
      clienteId: userId,
      planIds: planIds,
      total: this.total
    };

    this.purchaseService.createPurchase(request).subscribe({
      next: (res) => {
        localStorage.removeItem('cart');
        this.loading = false;
        // The purchase is now in PENDIENTE or VALIDANDO_SNS
        // Usually, we would wait or poll, but let's go to status page.
        this.router.navigate(['/status'], { queryParams: { id: res.id } });
      },
      error: (err) => {
        this.loading = false;
        alert('Error al crear compra');
      }
    });
  }
}
""",

    "checkout/checkout.component.html": """<div class="checkout-container">
  <h2>Confirmar Compra</h2>
  <div class="summary-card">
    <h3>Resumen</h3>
    <ul class="summary-list">
      <li *ngFor="let p of cart">
        <span>{{ p.nombre }}</span>
        <strong>${{ p.precio }}</strong>
      </li>
    </ul>
    <div class="summary-total">
      <span>Total a Pagar:</span>
      <strong>${{ total }}</strong>
    </div>
    <button (click)="confirmPurchase()" class="btn btn-success" [disabled]="loading">
      {{ loading ? 'Procesando...' : 'Confirmar y Enviar a SNS' }}
    </button>
  </div>
</div>
""",

    "checkout/checkout.component.css": """
.checkout-container { display: flex; flex-direction: column; align-items: center; }
.summary-card { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); width: 100%; max-width: 500px; margin-top: 1.5rem; }
h3 { color: #0f4c75; margin-bottom: 1.5rem; }
.summary-list { list-style: none; padding: 0; margin: 0; }
.summary-list li { display: flex; justify-content: space-between; padding: 0.75rem 0; border-bottom: 1px solid #eee; }
.summary-total { display: flex; justify-content: space-between; padding: 1.5rem 0; font-size: 1.25rem; color: #1b262c; border-top: 2px solid #3282b8; margin-top: 1rem; font-weight: bold; }
.btn-success { width: 100%; padding: 1rem; background-color: #28a745; color: white; border: none; border-radius: 6px; font-size: 1.1rem; font-weight: bold; cursor: pointer; transition: background 0.3s; }
.btn-success:hover { background-color: #218838; }
.btn-success:disabled { background-color: #94d3a2; cursor: not-allowed; }
""",

    "status/status.component.ts": """import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PurchaseService } from '../services/purchase.service';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.css']
})
export class StatusComponent implements OnInit {
  purchaseId: number | null = null;
  purchase: any = null;
  polling: any;

  constructor(
    private route: ActivatedRoute, 
    private purchaseService: PurchaseService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['id']) {
        this.purchaseId = Number(params['id']);
        this.checkStatus();
        this.polling = setInterval(() => this.checkStatus(), 3000); // Poll every 3 seconds
      }
    });
  }

  checkStatus() {
    if (!this.purchaseId) return;
    this.purchaseService.getPurchase(this.purchaseId).subscribe(res => {
      this.purchase = res;
      if (this.purchase.estado === 'PENDIENTE_PAGO' || this.purchase.estado === 'RECHAZADO' || this.purchase.estado === 'PAGADO' || this.purchase.estado === 'ERROR_SNS') {
        clearInterval(this.polling);
      }
    });
  }

  goToSaludPay() {
    this.router.navigate(['/saludpay'], { queryParams: { id: this.purchase.id, total: this.purchase.total } });
  }

  ngOnDestroy() {
    if (this.polling) clearInterval(this.polling);
  }
}
""",

    "status/status.component.html": """<div class="status-container">
  <h2>Estado de la Compra</h2>
  <div *ngIf="!purchaseId">
    <p>No se ha especificado una compra.</p>
  </div>
  <div *ngIf="purchase" class="status-card">
    <div class="status-header">
      <h3>Compra #{{ purchase.id }}</h3>
      <span class="badge" [ngClass]="purchase.estado">{{ purchase.estado }}</span>
    </div>
    
    <div class="status-details">
      <p><strong>Total:</strong> ${{ purchase.total }}</p>
      <p><strong>Fecha:</strong> {{ purchase.createdAt | date:'medium' }}</p>
      <p *ngIf="purchase.snsResult"><strong>Respuesta SNS:</strong> {{ purchase.snsResult }}</p>
    </div>

    <div *ngIf="purchase.estado === 'VALIDANDO_SNS'" class="alert alert-info">
      Esperando validación de la Superintendencia Nacional de Salud...
    </div>

    <div *ngIf="purchase.estado === 'PENDIENTE_PAGO'" class="alert alert-warning">
      SNS aprobó la compra. Pendiente de pago en Salud Pay.
      <button (click)="goToSaludPay()" class="btn btn-saludpay">Ir a Salud Pay</button>
    </div>

    <div *ngIf="purchase.estado === 'PAGADO'" class="alert alert-success">
      ¡Pago completado exitosamente! Los servicios han sido notificados (SHC, SAM).
    </div>

    <div *ngIf="purchase.estado === 'RECHAZADO' || purchase.estado === 'ERROR_SNS'" class="alert alert-danger">
      La compra no pudo ser procesada. Estado final: {{ purchase.estado }}.
    </div>
  </div>
</div>
""",

    "status/status.component.css": """
.status-container { max-width: 600px; margin: 0 auto; }
.status-card { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); margin-top: 1.5rem; }
.status-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 1rem; margin-bottom: 1rem; }
.status-header h3 { margin: 0; color: #0f4c75; }
.badge { padding: 0.5rem 1rem; border-radius: 20px; font-weight: bold; font-size: 0.9rem; }
.VALIDANDO_SNS, .PENDIENTE { background-color: #e2e3e5; color: #383d41; }
.PENDIENTE_PAGO { background-color: #fff3cd; color: #856404; }
.PAGADO { background-color: #d4edda; color: #155724; }
.RECHAZADO, .ERROR_SNS { background-color: #f8d7da; color: #721c24; }
.status-details p { margin: 0.5rem 0; font-size: 1.1rem; }
.alert { padding: 1rem; border-radius: 6px; margin-top: 1.5rem; }
.alert-info { background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
.alert-warning { background-color: #fff3cd; color: #856404; border: 1px solid #ffeeba; }
.alert-success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
.alert-danger { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
.btn-saludpay { width: 100%; margin-top: 1rem; padding: 1rem; background-color: #f39c12; color: white; border: none; border-radius: 6px; font-weight: bold; font-size: 1.1rem; cursor: pointer; }
.btn-saludpay:hover { background-color: #e67e22; }
""",

    "saludpay/saludpay.component.ts": """import { Component, OnInit } from '@angular/core';
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
""",

    "saludpay/saludpay.component.html": """<div class="sp-container">
  <div class="sp-card">
    <div class="sp-logo">Salud Pay</div>
    <div *ngIf="!success">
      <p class="sp-text">Estás a un paso de confirmar tu compra de salud.</p>
      <div class="sp-amount">${{ total }}</div>
      <button (click)="pagar()" class="btn btn-pay" [disabled]="loading">
        {{ loading ? 'Procesando pago...' : 'Pagar Ahora' }}
      </button>
    </div>
    <div *ngIf="success" class="sp-success">
      <div class="icon">✓</div>
      <h3>Pago Exitoso</h3>
      <p>Redirigiendo a tu compra...</p>
    </div>
  </div>
</div>
""",

    "saludpay/saludpay.component.css": """
.sp-container { display: flex; justify-content: center; align-items: center; min-height: 80vh; background-color: #2c3e50; margin: -2rem; padding: 2rem; border-radius: 8px; }
.sp-card { background: white; padding: 3rem; border-radius: 12px; box-shadow: 0 15px 30px rgba(0,0,0,0.2); width: 400px; text-align: center; }
.sp-logo { font-size: 2rem; font-weight: 900; color: #f39c12; margin-bottom: 2rem; letter-spacing: -1px; }
.sp-text { color: #7f8c8d; margin-bottom: 1rem; }
.sp-amount { font-size: 3rem; font-weight: bold; color: #2c3e50; margin-bottom: 2rem; }
.btn-pay { width: 100%; padding: 1.25rem; font-size: 1.25rem; background-color: #27ae60; color: white; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; transition: transform 0.1s, background-color 0.3s; }
.btn-pay:hover:not(:disabled) { background-color: #2ecc71; transform: scale(1.02); }
.sp-success { color: #27ae60; }
.sp-success .icon { font-size: 4rem; font-weight: bold; line-height: 1; }
.sp-success h3 { margin: 1rem 0 0.5rem; color: #2c3e50; }
"""
}

for rel_path, content in files.items():
    full_path = os.path.join(base_dir, rel_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content)

print("Angular files generated successfully.")
