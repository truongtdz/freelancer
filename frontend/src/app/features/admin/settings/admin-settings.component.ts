import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

interface Setting {
  key: string;
  label: string;
  unit: string;
  value: string;
  editing: boolean;
  editValue: string;
  saving: boolean;
}

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent],
  template: `
    <div>
      <h1 class="text-xl font-bold text-gray-900 mb-2">Cài đặt hệ thống</h1>
      <p class="text-sm text-gray-500 mb-6">Cập nhật các tham số vận hành của nền tảng.</p>

      @if (loading()) {
        <div class="flex justify-center py-20"><app-loading-spinner size="lg" /></div>
      } @else {
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-100 bg-gray-50">
                <th class="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Khoá</th>
                <th class="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Ý nghĩa</th>
                <th class="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Giá trị</th>
                <th class="px-5 py-3 w-32"></th>
              </tr>
            </thead>
            <tbody>
              @for (s of settings(); track s.key) {
                <tr class="border-b border-gray-50">
                  <td class="px-5 py-3.5 font-mono text-xs text-gray-600 align-middle">{{ s.key }}</td>
                  <td class="px-5 py-3.5 text-gray-700 align-middle">{{ s.label }}</td>
                  <td class="px-5 py-3.5 align-middle">
                    @if (s.editing) {
                      <div class="flex items-center gap-2">
                        <input [(ngModel)]="s.editValue" type="text"
                               class="px-2 py-1 text-sm border border-indigo-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-400 w-28"/>
                        @if (s.unit) {
                          <span class="text-sm text-gray-500">{{ s.unit }}</span>
                        }
                      </div>
                    } @else {
                      <span class="text-gray-900 font-medium">{{ s.value }}</span>
                      @if (s.unit) {
                        <span class="text-sm text-gray-400 ml-1">{{ s.unit }}</span>
                      }
                    }
                  </td>
                  <td class="px-5 py-3.5 text-right align-middle">
                    @if (s.editing) {
                      <div class="flex gap-2 justify-end">
                        <button (click)="cancelEdit(s)"
                                class="px-3 py-1 text-xs border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">
                          Huỷ
                        </button>
                        <button (click)="save(s)" [disabled]="s.saving"
                                class="px-3 py-1 text-xs bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60">
                          {{ s.saving ? 'Đang lưu...' : 'Lưu' }}
                        </button>
                      </div>
                    } @else {
                      <button (click)="startEdit(s)"
                              class="px-3 py-1 text-xs border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50">
                        Sửa
                      </button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `
})
export class AdminSettingsComponent implements OnInit {
  private adminSvc = inject(AdminService);
  private toast    = inject(ToastService);

  // Hard-coded known settings (BE doesn't have a GET /settings endpoint yet)
  KNOWN_SETTINGS = [
    { key: 'COMMISSION_RATE',         label: 'Tỉ lệ hoa hồng',                  unit: '%',   value: '10' },
    { key: 'MAX_SUBMISSION_ATTEMPTS', label: 'Số lần nộp tối đa',               unit: 'lần', value: '3'  },
    { key: 'CONTRACT_CANCEL_HOURS',   label: 'Timeout thanh toán hợp đồng',     unit: 'giờ', value: '24' },
    { key: 'AUTO_CONFIRM_DAYS',       label: 'Tự động xác nhận hoàn thành sau', unit: 'ngày',value: '7'  },
  ];

  settings = signal<Setting[]>([]);
  loading  = signal(true);

  ngOnInit() {
    // Load from known list (no GET endpoint yet)
    this.settings.set(this.KNOWN_SETTINGS.map(s => ({
      ...s,
      editing: false,
      editValue: s.value,
      saving: false,
    })));
    this.loading.set(false);
  }

  startEdit(s: Setting) {
    s.editValue = s.value;
    s.editing   = true;
  }

  cancelEdit(s: Setting) {
    s.editing = false;
  }

  save(s: Setting) {
    if (!s.editValue.trim()) return;
    s.saving = true;
    this.adminSvc.updateSetting(s.key, s.editValue.trim()).subscribe({
      next: () => {
        s.value   = s.editValue;
        s.editing = false;
        s.saving  = false;
        this.toast.success(`Đã cập nhật ${s.key}`);
      },
      error: err => {
        s.saving = false;
        this.toast.error(err?.message || 'Lưu thất bại');
      }
    });
  }
}
