import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { PendingPayoutItem } from '../../../core/models/admin.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { LucideAngularModule, CircleCheck, X } from 'lucide-angular';

@Component({
  selector: 'app-admin-payouts',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyVndPipe, TimeAgoPipe, LoadingSpinnerComponent, LucideAngularModule],
  template: `
    <div>
      <h1 class="text-xl font-bold text-gray-900 mb-6">Thanh toán chờ xử lý</h1>

      @if (loading()) {
        <div class="flex justify-center py-20"><app-loading-spinner size="lg" /></div>
      } @else if (!items().length) {
        <div class="text-center py-16 text-gray-400">
          <lucide-icon [img]="icons.CircleCheck" [size]="48" class="mx-auto mb-3 text-green-300"></lucide-icon>
          <p class="text-sm font-medium">Không có payout nào đang chờ</p>
        </div>
      } @else {
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-100 bg-gray-50">
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Hợp đồng</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Người làm</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Người thuê</th>
                <th class="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Giá</th>
                <th class="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Hoa hồng</th>
                <th class="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Tiền nhận</th>
                <th class="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Xác nhận</th>
                <th class="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              @for (item of items(); track item.contractId) {
                <tr class="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                  <td class="px-4 py-3 font-mono text-xs text-indigo-600">{{ item.contractCode }}</td>
                  <td class="px-4 py-3 text-gray-700">{{ item.freelancer?.fullName }}</td>
                  <td class="px-4 py-3 text-gray-500">{{ item.client?.fullName }}</td>
                  <td class="px-4 py-3 text-right font-medium text-gray-900">{{ item.agreedPrice | currencyVnd }}</td>
                  <td class="px-4 py-3 text-right text-orange-500">{{ item.commissionAmount | currencyVnd }}</td>
                  <td class="px-4 py-3 text-right font-semibold text-emerald-600">{{ item.netAmount | currencyVnd }}</td>
                  <td class="px-4 py-3 text-right text-xs text-gray-400">{{ item.confirmedAt | timeAgo }}</td>
                  <td class="px-4 py-3 text-right">
                    <button (click)="openDialog(item)"
                            class="px-3 py-1.5 bg-indigo-600 text-white text-xs font-semibold rounded-lg hover:bg-indigo-700 transition-colors">
                      Thanh toán
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>

    <!-- Pay dialog -->
    @if (selected()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
        <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-gray-900">Xác nhận thanh toán</h3>
            <button (click)="selected.set(null)" class="text-gray-400 hover:text-gray-600 p-1 rounded hover:bg-gray-100">
              <lucide-icon [img]="icons.X" [size]="16"></lucide-icon>
            </button>
          </div>

          <div class="space-y-4">
            <!-- Contract info -->
            <div class="bg-gray-50 rounded-lg p-3 text-sm">
              <div class="flex justify-between mb-1">
                <span class="text-gray-500">Hợp đồng</span>
                <span class="font-mono text-indigo-600">{{ selected()!.contractCode }}</span>
              </div>
              <div class="flex justify-between mb-1">
                <span class="text-gray-500">Freelancer</span>
                <span class="font-medium">{{ selected()!.freelancer?.fullName }}</span>
              </div>
              <div class="flex justify-between mb-1">
                <span class="text-gray-500">Số tiền net</span>
                <span class="font-bold text-emerald-600">{{ selected()!.netAmount | currencyVnd }}</span>
              </div>
            </div>

            <!-- Bank info -->
            @if (bankInfo()) {
              <div class="bg-blue-50 rounded-lg p-3 text-sm">
                <p class="text-xs font-semibold text-blue-600 mb-2">Thông tin chuyển khoản</p>
                <p class="text-gray-700"><span class="text-gray-500">Ngân hàng:</span> {{ bankInfo()!['bankName'] }}</p>
                <p class="text-gray-700"><span class="text-gray-500">Số TK:</span> <span class="font-mono font-medium">{{ bankInfo()!['accountNumber'] }}</span></p>
                <p class="text-gray-700"><span class="text-gray-500">Chủ TK:</span> {{ bankInfo()!['accountHolder'] }}</p>
              </div>
            }

            <!-- QR Code -->
            @if (selected()!.qrCodeUrl) {
              <div class="text-center">
                <p class="text-xs text-gray-500 mb-2">QR chuyển khoản (scan để chuyển)</p>
                <img [src]="selected()!.qrCodeUrl" alt="QR Code"
                     class="w-40 h-40 object-contain mx-auto rounded-lg border border-gray-200"/>
              </div>
            } @else {
              <div class="text-center py-4 text-gray-400 text-sm bg-gray-50 rounded-lg">
                Không có QR code
              </div>
            }

            <!-- Upload proof -->
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1.5">
                Ảnh chứng từ chuyển khoản *
              </label>
              @if (proofImageUrl()) {
                <div class="relative mb-2">
                  <img [src]="proofImageUrl()" class="w-full max-h-48 object-cover rounded-lg"/>
                  <button (click)="proofImageUrl.set('')"
                          class="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center hover:bg-red-600">
                    <lucide-icon [img]="icons.X" [size]="12"></lucide-icon>
                  </button>
                </div>
              }
              <label class="flex items-center justify-center gap-2 border-2 border-dashed border-gray-300 rounded-lg p-4 cursor-pointer hover:border-indigo-400 transition-colors">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                </svg>
                <span class="text-sm text-gray-500">{{ uploading() ? 'Đang tải...' : 'Chọn ảnh chứng từ' }}</span>
                <input type="file" accept="image/*" class="hidden" [disabled]="uploading()"
                       (change)="onProofFile($event)"/>
              </label>
            </div>

            <!-- Note -->
            <div>
              <label class="block text-xs font-medium text-gray-700 mb-1.5">Ghi chú (tuỳ chọn)</label>
              <textarea [(ngModel)]="note" rows="2" placeholder="VD: Đã chuyển khoản lúc 14:00..."
                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-400"></textarea>
            </div>

            <!-- Actions -->
            <div class="flex gap-2 pt-2">
              <button (click)="selected.set(null)"
                      class="flex-1 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50">
                Huỷ
              </button>
              <button (click)="confirmPayout()"
                      [disabled]="!proofImageUrl() || paying()"
                      class="flex-1 py-2 bg-indigo-600 text-white text-sm font-semibold rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                {{ paying() ? 'Đang xử lý...' : 'Xác nhận đã chuyển khoản' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `
})
export class AdminPayoutsComponent implements OnInit {
  private adminSvc  = inject(AdminService);
  private fileSvc   = inject(FileUploadService);
  private toast     = inject(ToastService);

  readonly icons = { CircleCheck, X };

  items    = signal<PendingPayoutItem[]>([]);
  loading  = signal(true);
  selected = signal<PendingPayoutItem | null>(null);
  bankInfo = signal<Record<string, string> | null>(null);

  proofImageUrl = signal('');
  note          = '';
  uploading     = signal(false);
  paying        = signal(false);

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.adminSvc.getPendingPayouts(0, 50).subscribe({
      next: p => { this.items.set(p.content); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openDialog(item: PendingPayoutItem) {
    this.selected.set(item);
    this.proofImageUrl.set('');
    this.note = '';
    // Parse bank info JSON
    if (item.bankInfoSnapshot) {
      try {
        this.bankInfo.set(JSON.parse(item.bankInfoSnapshot));
      } catch { this.bankInfo.set(null); }
    } else {
      this.bankInfo.set(null);
    }
  }

  onProofFile(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.uploading.set(true);
    this.fileSvc.uploadFile(file, 'payment-proof').subscribe({
      next: res => { this.proofImageUrl.set(res.url); this.uploading.set(false); },
      error: () => { this.uploading.set(false); this.toast.error('Upload thất bại'); }
    });
  }

  confirmPayout() {
    const item = this.selected();
    if (!item || !this.proofImageUrl()) return;
    this.paying.set(true);
    this.adminSvc.payContract(item.contractId, {
      proofImageUrl: this.proofImageUrl(),
      note: this.note || undefined
    }).subscribe({
      next: () => {
        this.toast.success('Đã xác nhận payout thành công!');
        this.paying.set(false);
        this.selected.set(null);
        this.load();
      },
      error: err => {
        this.paying.set(false);
        this.toast.error(err?.message || 'Payout thất bại');
      }
    });
  }
}
