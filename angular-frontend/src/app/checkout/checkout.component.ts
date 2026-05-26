import { Component, OnInit } from '@angular/core';
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
  error: string = '';

  constructor(private purchaseService: PurchaseService, private router: Router) {}

  ngOnInit(): void {
    this.cart = JSON.parse(localStorage.getItem('cart') || '[]');
    this.total = this.cart.reduce((sum, p) => sum + p.precio, 0);
  }

  confirmPurchase() {
    if (this.cart.length === 0) {
      this.error = 'El carrito esta vacio';
      return;
    }

    this.loading = true;
    this.error = '';

    const planIds = this.cart.map(p => p.id);
    const userId  = Number(localStorage.getItem('userId'));
    const cedula  = localStorage.getItem('cedula') || '';

    const request = {
      clienteId: userId,
      cedula: cedula,
      planIds: planIds,
      total: this.total
    };

    this.purchaseService.createPurchase(request).subscribe({
      next: (res: any) => {
        localStorage.removeItem('cart');
        this.loading = false;
        this.router.navigate(['/status'], { queryParams: { id: res.id } });
      },
      error: (err: any) => {
        this.loading = false;
        this.error = 'Error al crear la compra. Intenta nuevamente.';
      }
    });
  }
}
