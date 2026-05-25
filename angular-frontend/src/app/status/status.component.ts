import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PurchaseService } from '../services/purchase.service';
import { CatalogService } from '../services/catalog.service';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.css']
})
export class StatusComponent implements OnInit {
  purchaseId: number | null = null;
  purchase: any = null;
  polling: any;
  purchases: any[] = [];
  loadingPurchases: boolean = false;
  planesMap: { [key: number]: any } = {};

  constructor(
    private route: ActivatedRoute, 
    private purchaseService: PurchaseService,
    private catalogService: CatalogService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPlanes();
    this.route.queryParams.subscribe(params => {
      if (params['id']) {
        this.purchaseId = Number(params['id']);
        this.checkStatus();
        if (this.polling) clearInterval(this.polling);
        this.polling = setInterval(() => this.checkStatus(), 3000); // Poll every 3 seconds
      } else {
        this.purchaseId = null;
        this.purchase = null;
        if (this.polling) clearInterval(this.polling);
        this.loadAllPurchases();
      }
    });
  }

  loadPlanes() {
    this.catalogService.getPlanes().subscribe({
      next: (res) => {
        this.planesMap = {};
        res.forEach((p: any) => {
          this.planesMap[p.id] = p;
        });
      }
    });
  }

  getPlanNames(payloadStr: string): string {
    if (!payloadStr) return 'Plan de Salud';
    try {
      const data = JSON.parse(payloadStr);
      const planIds = data.planIds || [];
      const names = planIds.map((id: number) => {
        const plan = this.planesMap[id];
        return plan ? plan.nombre : `Plan #${id}`;
      });
      return names.join(', ') || 'Sin planes';
    } catch (e) {
      return 'Plan de Salud';
    }
  }

  getPlanServices(payloadStr: string): any[] {
    if (!payloadStr) return [];
    try {
      const data = JSON.parse(payloadStr);
      const planIds = data.planIds || [];
      const servicesList: any[] = [];
      planIds.forEach((id: number) => {
        const plan = this.planesMap[id];
        if (plan && plan.servicios) {
          plan.servicios.forEach((s: any) => {
            if (!servicesList.some(item => item.id === s.id)) {
              servicesList.push(s);
            }
          });
        }
      });
      return servicesList;
    } catch (e) {
      return [];
    }
  }

  loadAllPurchases() {
    const userId = Number(localStorage.getItem('userId'));
    if (!userId) return;
    this.loadingPurchases = true;
    this.purchaseService.getPurchasesByCliente(userId).subscribe({
      next: (res) => {
        this.purchases = res;
        this.loadingPurchases = false;
      },
      error: () => {
        this.loadingPurchases = false;
      }
    });
  }

  viewPurchase(id: number) {
    this.router.navigate(['/status'], { queryParams: { id } });
  }

  goToSaludPaySpecific(p: any) {
    this.router.navigate(['/saludpay'], { queryParams: { id: p.id, total: p.total } });
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
