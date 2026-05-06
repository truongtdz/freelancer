import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { AdminDispute, DisputeStatus, DisputeResolutionType } from '../../../core/models/admin.model';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { LucideAngularModule, CircleCheck, X } from 'lucide-angular';

@Component({
  selector: 'app-admin-disputes',
  standalone: true,
  imports: [CommonModule, FormsModule, TimeAgoPipe, CurrencyVndPipe, LoadingSpinnerComponent, LucideAngularModule],
  template: `
    <div>
      <h1 class="text-xl font-bold text-gray-900 mb-6">Quản lý tranh chấp</h1>

      <!-- Status tabs -->
      <div class="flex gap-1 border-b border-gray-200 mb-6">
        @for (tab of tabs; track tab.value) {
          <button (click)="setTab(tab.value)"
                  class="px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors whitespace-nowrap"
                  [class.border-indigo-600]="activeTab() === tab.value"
                  [class.text-indigo-600]="activeTab() === tab.value"
                  [class.border-transparent]="activeTab() !== tab.value"
                  [class.text-gray-500]="activeTab() !== tab.value">
            {{ tab.label }}
          </button>
        }
      </div>

      @if (loading()) {
        <div class="flex justify-center py-20"><app-loading-spinner size="lg" /></div>
      } @else if (!items().length) {
        <div class="text-center py-16 text-gray-400">
          <lucide-icon [img]="icons.CircleCheck" [size]="48" class="mx-auto mb-3 text-green-300"></lucide-icon>
          <p class="text-sm">Không có tranh chấp nào</p>
        </div>
      } @else {
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-100 bg-gray-50">
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">HĐ #</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Lý do</th>
                <th class="text-center px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Trạng thái</th>
                <th class="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Ngày mở</th>
                <th class="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              @for (d of items(); track d.id) {
                <tr class="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                  <td class="px-4 py-3 font-mono text-xs text-indigo-600">{{ d.contractId }}</td>
                  <td class="px-4 py-3 text-gray-700 max-w-xs truncate">{{ d.reason }}</td>
                  <td class="px-4 py-3 text-center">
                    <span [class]="statusClass(d.status) + ' px-2 py-0.5 rounded-full text-xs font-medium'">
                      {{ statusLabel(d.status) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-right text-xs text-gray-400">{{ d.createdAt | timeAgo }}</td>
                  <td class="px-4 py-3 text-right">
                    <button (click)="openDialog(d)"
                            [disabled]="d.status === 'RESOLVED' || d.status === 'CLOSED'"
                            class="px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors"
                            [class.bg-indigo-600]="d.status === 'OPEN' || d.status === 'IN_REVIEW'"
                            [class.text-white]="d.status === 'OPEN' || d.status === 'IN_REVIEW'"
                            [class.hover:bg-indigo-700]="d.status === 'OPEN' || d.status === 'IN_REVIEW'"
                            [class.bg-gray-100]="d.status === 'RESOLVED' || d.status === 'CLOSED'"
                            [class.text-gray-400]="d.status === 'RESOLVED' || d.status === 'CLOSED'">
                      {{ d.status === 'RESOLVED' || d.status === 'CLOSED' ? 'Đã xử lý' : 'Giải quyết' }}
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>

    <!-- Resolve dialog -->
    @if (selected()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
        <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-gray-900">Giải quyết tranh chấp</h3>
            <button (click)="selected.set(null)" class="text-gray-400 hover:text-gray-600 p-1 rounded hover:bg-gray-100">
              <lucide-icon [img]="icons.X" [size]="16"></lucide-icon>
            </button>
          </div>

          <!-- Dispute detail -->
          <div class="bg-red-50 border border-red-200 rounded-lg p-4 mb-5">
            <p class="text-sm font-semibold text-red-700 mb-1">{{ selected()!.reason }}</p>
            <p class="text-sm text-red-600">{{ selected()!.description }}</p>
            <p class="text-xs text-gray-400 mt-2">
              Hợp đồng ID: {{ selected()!.contractId }} · {{ selected()!.createdAt | timeAgo }}
            </p>
          </div>

          <div class="space-y-4">
            <!-- Resolution type -->
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-2 uppercase tracking-wide">
                Hình thức giải quyết *
              </label>
              <div class="space-y-2">
                @for (opt of resolutionOptions; track opt.value) {
                  <label class="flex items-start gap-3 p-3 border rounded-lg cursor-pointer transition-colors"
                         [class.border-indigo-400]="resolutionType === opt.value"
                         [class.bg-indigo-50]="resolutionType === opt.value"
                         [class.border-gray-200]="resolutionType !== opt.value">
                    <input type="radio" [value]="opt.value" [(ngModel)]="resolutionType"
                           class="mt-0.5 accent-indigo-600"/>
                    <div>
                      <p class="text-sm font-medium text-gray-900">{{ opt.label }}</p>
                      <p class="text-xs text-gray-500">{{ opt.desc }}</p>
                    </div>
                  </label>
                }
              </div>
            </div>

            <!-- Partial amount -->
            @if (resolutionType === 'PARTIAL') {
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1.5">
                  Số tiền cho Freelancer (VND) *
                </label>
                <input [(ngModel)]="partialAmount" type="number" min="0" placeholder="VD: 5000000"
                       class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-400"/>
                <p class="text-xs text-gray-400 mt-1">Phần còn lại sẽ được hoàn cho Client</p>
              </div>
            }

            <!-- Resolution text -->
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1.5">
                Ghi chú phán quyết * (tối thiểu 10 ký tự)
              </label>
              <textarea [(ngModel)]="resolution" rows="3"
                        placeholder="Giải thích lý do phán quyết..."
                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-400"></textarea>
            </div>

            <!-- Actions -->
            <div class="flex gap-2 pt-1">
              <button (click)="selected.set(null)"
                      class="flex-1 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50">
                Huỷ
              </button>
              <button (click)="confirmResolve()"
                      [disabled]="resolving() || !resolution.trim() || resolution.length < 10"
                      class="flex-1 py-2 bg-indigo-600 text-white text-sm font-semibold rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                {{ resolving() ? 'Đang xử lý...' : 'Xác nhận phán quyết' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `
})
export class AdminDisputesComponent implements OnInit {
  private adminSvc = inject(AdminService);
  private toast    = inject(ToastService);

  readonly icons = { CircleCheck, X };

  tabs: { value: DisputeStatus | ''; label: string }[] = [
    { value: 'OPEN',      label: 'Đang mở' },
    { value: 'IN_REVIEW', label: 'Đang xem xét' },
    { value: 'RESOLVED',  label: 'Đã giải quyết' },
    { value: 'CLOSED',    label: 'Đã đóng' },
  ];

  activeTab = signal<DisputeStatus | ''>('OPEN');
  items     = signal<AdminDispute[]>([]);
  loading   = signal(true);
  selected  = signal<AdminDispute | null>(null);

  resolutionType: DisputeResolutionType = 'FULL_REFUND';
  partialAmount = 0;
  resolution    = '';
  resolving     = signal(false);

  resolutionOptions: { value: DisputeResolutionType; label: string; desc: string }[] = [
    { value: 'FULL_REFUND',  label: 'Hoàn tiền toàn bộ cho Client', desc: 'Client được hoàn 100% số tiền' },
    { value: 'FULL_PAYOUT',  label: 'Trả toàn bộ cho Freelancer',   desc: 'Freelancer nhận đủ net amount' },
    { value: 'PARTIAL',      label: 'Chia sẻ một phần',              desc: 'Trả một phần cho Freelancer, hoàn phần còn lại cho Client' },
  ];

  ngOnInit() { this.load(); }

  setTab(tab: DisputeStatus | '') {
    this.activeTab.set(tab);
    this.load();
  }

  load() {
    this.loading.set(true);
    const status = this.activeTab() as DisputeStatus | undefined;
    this.adminSvc.getDisputes(status || undefined, 0, 50).subscribe({
      next: p => { this.items.set(p.content); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openDialog(d: AdminDispute) {
    this.selected.set(d);
    this.resolutionType = 'FULL_REFUND';
    this.partialAmount  = 0;
    this.resolution     = '';
  }

  confirmResolve() {
    const d = this.selected();
    if (!d || !this.resolution.trim()) return;
    this.resolving.set(true);
    this.adminSvc.resolveDispute(d.id, {
      resolutionType: this.resolutionType,
      partialAmountToFreelancer: this.resolutionType === 'PARTIAL' ? this.partialAmount : undefined,
      resolution: this.resolution.trim()
    }).subscribe({
      next: () => {
        this.toast.success('Tranh chấp đã được giải quyết');
        this.resolving.set(false);
        this.selected.set(null);
        this.load();
      },
      error: err => {
        this.resolving.set(false);
        this.toast.error(err?.message || 'Giải quyết thất bại');
      }
    });
  }

  statusLabel(s: DisputeStatus): string {
    return { OPEN: 'Đang mở', IN_REVIEW: 'Đang xem', RESOLVED: 'Đã giải quyết', CLOSED: 'Đã đóng' }[s] ?? s;
  }

  statusClass(s: DisputeStatus): string {
    return {
      OPEN:      'bg-red-100 text-red-700',
      IN_REVIEW: 'bg-yellow-100 text-yellow-700',
      RESOLVED:  'bg-green-100 text-green-700',
      CLOSED:    'bg-gray-100 text-gray-500'
    }[s] ?? '';
  }
}
