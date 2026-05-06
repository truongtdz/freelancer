import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { LucideAngularModule, X } from 'lucide-angular';

const STATUS_TABS = [
  { value: '',                     label: 'Tất cả' },
  { value: 'PENDING_PAYMENT',      label: 'Chờ thanh toán' },
  { value: 'IN_PROGRESS',          label: 'Đang thực hiện' },
  { value: 'FREELANCER_SUBMITTED', label: 'Chờ xác nhận' },
  { value: 'CLIENT_CONFIRMED',     label: 'Đã xác nhận' },
  { value: 'PAID_OUT',             label: 'Đã thanh toán' },
  { value: 'DISPUTED',             label: 'Tranh chấp' },
  { value: 'CANCELLED',            label: 'Đã huỷ' },
];

const STATUS_STYLES: Record<string, string> = {
  PENDING_PAYMENT:      'bg-yellow-100 text-yellow-700',
  IN_PROGRESS:          'bg-blue-100 text-blue-700',
  FREELANCER_SUBMITTED: 'bg-purple-100 text-purple-700',
  CLIENT_CONFIRMED:     'bg-cyan-100 text-cyan-700',
  PAID_OUT:             'bg-green-100 text-green-700',
  DISPUTED:             'bg-red-100 text-red-700',
  CANCELLED:            'bg-gray-100 text-gray-500',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING_PAYMENT:      'Chờ thanh toán',
  IN_PROGRESS:          'Đang thực hiện',
  FREELANCER_SUBMITTED: 'Chờ xác nhận',
  CLIENT_CONFIRMED:     'Đã xác nhận',
  PAID_OUT:             'Đã thanh toán',
  DISPUTED:             'Tranh chấp',
  CANCELLED:            'Đã huỷ',
};

@Component({
  selector: 'app-admin-contracts',
  standalone: true,
  imports: [CommonModule, LoadingSpinnerComponent, LucideAngularModule],
  template: `
    <div>
      <h1 class="text-xl font-bold text-gray-900 mb-2">Quản lý hợp đồng</h1>
      <p class="text-sm text-gray-500 mb-5">Xem và theo dõi tất cả hợp đồng trong hệ thống.</p>

      <!-- Status tabs -->
      <div class="flex gap-1 flex-wrap mb-5">
        @for (tab of statusTabs; track tab.value) {
          <button (click)="selectStatus(tab.value)"
                  [class.bg-indigo-600]="activeStatus() === tab.value"
                  [class.text-white]="activeStatus() === tab.value"
                  [class.bg-white]="activeStatus() !== tab.value"
                  [class.text-gray-600]="activeStatus() !== tab.value"
                  class="px-3 py-1.5 text-xs rounded-lg border border-gray-200 hover:border-indigo-400 transition-colors font-medium">
            {{ tab.label }}
          </button>
        }
      </div>

      @if (loading()) {
        <div class="flex justify-center py-20"><app-loading-spinner size="lg" /></div>
      } @else {
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table class="w-full text-sm min-w-[900px]">
            <thead>
              <tr class="border-b border-gray-100 bg-gray-50">
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Mã HĐ</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Công việc</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Khách hàng</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Freelancer</th>
                <th class="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Giá trị</th>
                <th class="text-center px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Trạng thái</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Ngày tạo</th>
                <th class="px-4 py-3 w-20"></th>
              </tr>
            </thead>
            <tbody>
              @if (contracts().length === 0) {
                <tr>
                  <td colspan="8" class="px-4 py-16 text-center text-gray-400 text-sm">
                    Không có hợp đồng nào
                  </td>
                </tr>
              }
              @for (c of contracts(); track c.id) {
                <tr class="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                  <td class="px-4 py-3 font-mono text-xs text-gray-600 whitespace-nowrap">{{ c.contractCode }}</td>
                  <td class="px-4 py-3 max-w-[180px]">
                    <span class="truncate block text-gray-800" [title]="c.jobTitle">{{ c.jobTitle ?? '—' }}</span>
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-2">
                      @if (c.client?.avatarUrl) {
                        <img [src]="c.client.avatarUrl" class="w-6 h-6 rounded-full object-cover shrink-0" />
                      } @else {
                        <div class="w-6 h-6 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 text-xs font-bold shrink-0">
                          {{ (c.client?.fullName ?? 'C')[0].toUpperCase() }}
                        </div>
                      }
                      <span class="text-gray-700 text-xs">{{ c.client?.fullName ?? '—' }}</span>
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-2">
                      @if (c.freelancer?.avatarUrl) {
                        <img [src]="c.freelancer.avatarUrl" class="w-6 h-6 rounded-full object-cover shrink-0" />
                      } @else {
                        <div class="w-6 h-6 rounded-full bg-green-100 flex items-center justify-center text-green-600 text-xs font-bold shrink-0">
                          {{ (c.freelancer?.fullName ?? 'F')[0].toUpperCase() }}
                        </div>
                      }
                      <span class="text-gray-700 text-xs">{{ c.freelancer?.fullName ?? '—' }}</span>
                    </div>
                  </td>
                  <td class="px-4 py-3 text-right whitespace-nowrap text-gray-800 font-medium">
                    {{ c.agreedPrice | number:'1.0-0' }} ₫
                  </td>
                  <td class="px-4 py-3 text-center">
                    <span class="px-2 py-0.5 rounded-full text-xs font-medium whitespace-nowrap"
                          [ngClass]="statusStyle(c.status)">
                      {{ statusLabel(c.status) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-gray-500 text-xs whitespace-nowrap">
                    {{ formatDate(c.createdAt) }}
                  </td>
                  <td class="px-4 py-3 text-right">
                    <button (click)="openDetail(c)"
                            class="px-3 py-1 text-xs border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">
                      Xem
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        @if (totalPages() > 1) {
          <div class="flex items-center justify-between mt-4 text-sm text-gray-600">
            <span>Trang {{ currentPage() + 1 }} / {{ totalPages() }} — {{ totalElements() }} hợp đồng</span>
            <div class="flex gap-2">
              <button (click)="changePage(currentPage() - 1)" [disabled]="currentPage() === 0"
                      class="px-3 py-1.5 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                ← Trước
              </button>
              <button (click)="changePage(currentPage() + 1)" [disabled]="currentPage() >= totalPages() - 1"
                      class="px-3 py-1.5 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                Sau →
              </button>
            </div>
          </div>
        }
      }
    </div>

    <!-- Detail Modal -->
    @if (selected()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4"
           (click)="closeDetail()">
        <div class="absolute inset-0 bg-black/40"></div>
        <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto"
             (click)="$event.stopPropagation()">

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <div>
              <h2 class="font-bold text-gray-900 text-base">Chi tiết hợp đồng</h2>
              <p class="text-xs text-gray-400 font-mono mt-0.5">{{ selected()!.contractCode }}</p>
            </div>
            <button (click)="closeDetail()"
                    class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors">
              <lucide-icon [img]="xIcon" [size]="16"></lucide-icon>
            </button>
          </div>

          <!-- Body -->
          <div class="px-6 py-5 space-y-5">

            <!-- Status badge -->
            <div class="flex items-center gap-2">
              <span class="px-3 py-1 rounded-full text-xs font-semibold"
                    [ngClass]="statusStyle(selected()!.status)">
                {{ statusLabel(selected()!.status) }}
              </span>
            </div>

            <!-- Job -->
            <div class="bg-gray-50 rounded-xl p-4">
              <p class="text-xs text-gray-400 mb-1">Công việc</p>
              <p class="text-sm font-medium text-gray-800">{{ selected()!.jobTitle ?? '—' }}</p>
            </div>

            <!-- Parties -->
            <div class="grid grid-cols-2 gap-3">
              <div class="bg-indigo-50 rounded-xl p-4">
                <p class="text-xs text-indigo-400 mb-2">Khách hàng</p>
                <div class="flex items-center gap-2">
                  @if (selected()!.client?.avatarUrl) {
                    <img [src]="selected()!.client.avatarUrl" class="w-8 h-8 rounded-full object-cover shrink-0" />
                  } @else {
                    <div class="w-8 h-8 rounded-full bg-indigo-200 flex items-center justify-center text-indigo-700 text-xs font-bold shrink-0">
                      {{ (selected()!.client?.fullName ?? 'C')[0].toUpperCase() }}
                    </div>
                  }
                  <div>
                    <p class="text-xs font-semibold text-gray-800">{{ selected()!.client?.fullName ?? '—' }}</p>
                    <p class="text-xs text-gray-400">{{ selected()!.client?.email ?? '' }}</p>
                  </div>
                </div>
              </div>
              <div class="bg-green-50 rounded-xl p-4">
                <p class="text-xs text-green-400 mb-2">Freelancer</p>
                <div class="flex items-center gap-2">
                  @if (selected()!.freelancer?.avatarUrl) {
                    <img [src]="selected()!.freelancer.avatarUrl" class="w-8 h-8 rounded-full object-cover shrink-0" />
                  } @else {
                    <div class="w-8 h-8 rounded-full bg-green-200 flex items-center justify-center text-green-700 text-xs font-bold shrink-0">
                      {{ (selected()!.freelancer?.fullName ?? 'F')[0].toUpperCase() }}
                    </div>
                  }
                  <div>
                    <p class="text-xs font-semibold text-gray-800">{{ selected()!.freelancer?.fullName ?? '—' }}</p>
                    <p class="text-xs text-gray-400">{{ selected()!.freelancer?.email ?? '' }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Financials -->
            <div class="border border-gray-100 rounded-xl overflow-hidden">
              <div class="bg-gray-50 px-4 py-2 text-xs font-semibold text-gray-500 uppercase">Tài chính</div>
              <div class="divide-y divide-gray-50">
                <div class="flex justify-between px-4 py-2.5 text-sm">
                  <span class="text-gray-500">Giá trị hợp đồng</span>
                  <span class="font-semibold text-gray-800">{{ selected()!.agreedPrice | number:'1.0-0' }} ₫</span>
                </div>
                <div class="flex justify-between px-4 py-2.5 text-sm">
                  <span class="text-gray-500">Hoa hồng ({{ selected()!.commissionRate }}%)</span>
                  <span class="text-orange-600">{{ selected()!.commissionAmount | number:'1.0-0' }} ₫</span>
                </div>
                <div class="flex justify-between px-4 py-2.5 text-sm">
                  <span class="text-gray-500">Freelancer nhận</span>
                  <span class="font-semibold text-green-600">{{ selected()!.netAmount | number:'1.0-0' }} ₫</span>
                </div>
              </div>
            </div>

            <!-- Dates -->
            <div class="grid grid-cols-2 gap-3">
              <div>
                <p class="text-xs text-gray-400 mb-0.5">Ngày bắt đầu</p>
                <p class="text-sm text-gray-700">{{ selected()!.startDate ?? '—' }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-400 mb-0.5">Ngày kết thúc</p>
                <p class="text-sm text-gray-700">{{ selected()!.endDate ?? '—' }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-400 mb-0.5">Ngày tạo</p>
                <p class="text-sm text-gray-700">{{ formatDate(selected()!.createdAt) }}</p>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
            <button (click)="closeDetail()"
                    class="px-4 py-2 text-sm border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">
              Đóng
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class AdminContractsComponent implements OnInit {
  private adminSvc = inject(AdminService);

  statusTabs    = STATUS_TABS;
  activeStatus  = signal('');
  contracts     = signal<any[]>([]);
  loading       = signal(true);
  currentPage   = signal(0);
  totalPages    = signal(0);
  totalElements = signal(0);
  selected      = signal<any>(null);
  readonly xIcon = X;

  ngOnInit() { this.load(); }

  selectStatus(value: string) {
    this.activeStatus.set(value);
    this.currentPage.set(0);
    this.load();
  }

  changePage(page: number) {
    this.currentPage.set(page);
    this.load();
  }

  load() {
    this.loading.set(true);
    const status = this.activeStatus() || undefined;
    this.adminSvc.getContracts(status, this.currentPage(), 20).subscribe({
      next: res => {
        this.contracts.set(res.content);
        this.totalPages.set(res.totalPages);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openDetail(c: any) { this.selected.set(c); }
  closeDetail()      { this.selected.set(null); }

  statusStyle(status: string): string {
    return STATUS_STYLES[status] ?? 'bg-gray-100 text-gray-500';
  }

  statusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
  }

  formatDate(dt: string): string {
    if (!dt) return '—';
    return new Date(dt).toLocaleDateString('vi-VN');
  }
}
