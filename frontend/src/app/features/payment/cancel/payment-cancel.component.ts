import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

const CANCEL_REASONS: Record<string, string> = {
  '24': 'Bạn đã huỷ giao dịch.',
  '51': 'Tài khoản không đủ số dư.',
  '65': 'Vượt hạn mức giao dịch trong ngày.',
  '75': 'Ngân hàng đang bảo trì.',
  'invalid_signature': 'Chữ ký không hợp lệ — vui lòng thử lại.',
};

@Component({
  selector: 'app-payment-cancel',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './payment-cancel.component.html'
})
export class PaymentCancelComponent implements OnInit {
  private route = inject(ActivatedRoute);

  code   = signal('');
  reason = signal('');

  ngOnInit() {
    const code   = this.route.snapshot.queryParams['code'] ?? '';
    const reason = this.route.snapshot.queryParams['reason'] ?? '';
    this.code.set(code);
    this.reason.set(CANCEL_REASONS[reason || code] ?? 'Thanh toán không thành công.');
  }
}
