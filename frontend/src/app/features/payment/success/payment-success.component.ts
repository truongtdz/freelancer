import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-success.component.html'
})
export class PaymentSuccessComponent implements OnInit {
  private route = inject(ActivatedRoute);
  txnRef = signal('');

  ngOnInit() {
    this.txnRef.set(this.route.snapshot.queryParams['txnRef'] ?? '');
  }
}
