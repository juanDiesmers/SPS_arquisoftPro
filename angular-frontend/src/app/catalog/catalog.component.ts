import { Component, OnInit } from '@angular/core';
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
