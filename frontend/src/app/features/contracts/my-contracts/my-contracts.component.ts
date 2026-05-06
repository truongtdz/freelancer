import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ContractService } from '../../../core/services/contract.service';
import { ContractListItem, ContractStatus } from '../../../core/models/contract.model';
import { PageResponse } from '../../../core/models/api.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ToastService } from '../../../shared/components/toast/toast.service';

type TabStatus = '' | ContractStatus;

@Component({
  selector: 'app-my-contracts',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyVndPipe, TimeAgoPipe,
            PaginationComponent, EmptyStateComponent, LoadingSpinnerComponent],
  templateUrl: './my-contracts.component.html'
})
export class MyContractsComponent implements OnInit {
  private contractSvc = inject(ContractService);
  private toast       = inject(ToastService);

  data      = signal<PageResponse<ContractListItem> | null>(null);
  loading   = signal(false);
  page      = signal(0);
  activeTab = signal<TabStatus>('');

  tabs: { value: TabStatus; label: string }[] = [
    { value: '',                     label: 'Tất cả' },
    { value: 'PENDING_PAYMENT',      label: 'Chờ thanh toán' },
    { value: 'IN_PROGRESS',          label: 'Đang thực hiện' },
    { value: 'FREELANCER_SUBMITTED', label: 'Chờ xác nhận' },
    { value: 'CLIENT_CONFIRMED',     label: 'Đã xác nhận' },
    { value: 'PAID_OUT',             label: 'Đã thanh toán' },
    { value: 'CANCELLED',            label: 'Đã huỷ' },
  ];

  payingId = signal<number | null>(null);

  ngOnInit() { this.load(); }

  setTab(tab: TabStatus) { this.activeTab.set(tab); this.page.set(0); this.load(); }
  onPageChange(p: number) { this.page.set(p); this.load(); }

  load() {
    this.loading.set(true);
    const status = this.activeTab() || undefined;
    this.contractSvc.getMyContracts(this.page(), 10, status as ContractStatus | undefined).subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  pay(c: ContractListItem) {
    this.payingId.set(c.id);
    this.contractSvc.initiatePayment(c.id).subscribe({
      next: ({ paymentUrl }) => { this.payingId.set(null); window.location.href = paymentUrl; },
      error: err => { this.payingId.set(null); this.toast.error(err?.message === 'Forbidden' ? 'Bạn không có quyền thanh toán' : 'Không thể tạo link thanh toán'); }
    });
  }

  statusLabel(s: ContractStatus): string {
    const map: Record<ContractStatus, string> = {
      PENDING_PAYMENT:      'Chờ thanh toán',
      IN_PROGRESS:          'Đang thực hiện',
      FREELANCER_SUBMITTED: 'Chờ xác nhận',
      CLIENT_CONFIRMED:     'Đã xác nhận',
      PAID_OUT:             'Đã thanh toán',
      DISPUTED:             'Đang tranh chấp',
      CANCELLED:            'Đã huỷ'
    };
    return map[s] ?? s;
  }

  statusClass(s: ContractStatus): string {
    const map: Partial<Record<ContractStatus, string>> = {
      PENDING_PAYMENT:      'bg-yellow-100 text-yellow-700',
      IN_PROGRESS:          'bg-blue-100 text-blue-700',
      FREELANCER_SUBMITTED: 'bg-purple-100 text-purple-700',
      CLIENT_CONFIRMED:     'bg-green-100 text-green-700',
      PAID_OUT:             'bg-emerald-100 text-emerald-700',
      DISPUTED:             'bg-red-100 text-red-600',
      CANCELLED:            'bg-gray-100 text-gray-500'
    };
    return map[s] ?? '';
  }
}
