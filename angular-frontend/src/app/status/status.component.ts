import { Component, OnInit } from '@angular/core';
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
