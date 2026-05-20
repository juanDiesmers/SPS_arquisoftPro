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
