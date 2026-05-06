import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { LucideAngularModule, Users } from 'lucide-angular';

interface AdminUser {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  status: string;
  createdAt: string;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, TimeAgoPipe, LoadingSpinnerComponent, LucideAngularModule],
  template: `
    <div>
      <h1 class="text-xl font-bold text-gray-900 mb-6">Quản lý người dùng</h1>

      <!-- Filters -->
      <div class="flex flex-wrap gap-3 mb-5">
        @for (tab of roleTabs; track tab.value) {
          <button (click)="setRole(tab.value)"
                  class="px-4 py-1.5 text-sm font-medium rounded-full border transition-colors"
                  [class.bg-indigo-600]="activeRole() === tab.value"
                  [class.text-white]="activeRole() === tab.value"
                  [class.border-indigo-600]="activeRole() === tab.value"
                  [class.bg-white]="activeRole() !== tab.value"
                  [class.text-gray-600]="activeRole() !== tab.value"
                  [class.border-gray-300]="activeRole() !== tab.value">
            {{ tab.label }}
          </button>
        }
      </div>

      @if (loading()) {
        <div class="flex justify-center py-20"><app-loading-spinner size="lg" /></div>
      } @else if (!users().length) {
        <div class="text-center py-16 text-gray-400">
          <lucide-icon [img]="usersIcon" [size]="48" class="mx-auto mb-3 text-gray-300"></lucide-icon>
          <p class="text-sm">Không có người dùng nào</p>
        </div>
      } @else {
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-100 bg-gray-50">
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Người dùng</th>
                <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Vai trò</th>
                <th class="text-center px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Trạng thái</th>
                <th class="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Tham gia</th>
                <th class="px-4 py-3 w-24"></th>
              </tr>
            </thead>
            <tbody>
              @for (u of users(); track u.id) {
                <tr class="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center shrink-0">
                        <span class="text-xs font-bold text-indigo-600">{{ u.fullName?.charAt(0)?.toUpperCase() }}</span>
                      </div>
                      <div class="min-w-0">
                        <p class="font-medium text-gray-900 truncate">{{ u.fullName }}</p>
                        <p class="text-xs text-gray-400 truncate">{{ u.email }}</p>
                      </div>
                    </div>
                  </td>
                  <td class="px-4 py-3">
                    <span [class]="roleClass(u.role) + ' px-2 py-0.5 rounded-full text-xs font-medium'">
                      {{ u.role }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-center">
                    <span [class]="statusClass(u.status) + ' px-2 py-0.5 rounded-full text-xs font-medium'">
                      {{ statusLabel(u.status) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-right text-xs text-gray-400">{{ u.createdAt | timeAgo }}</td>
                  <td class="px-4 py-3 text-right">
                    <button (click)="toggleStatus(u)"
                            class="px-2.5 py-1 text-xs border rounded-lg transition-colors"
                            [class.border-red-200]="u.status === 'ACTIVE'"
                            [class.text-red-600]="u.status === 'ACTIVE'"
                            [class.hover:bg-red-50]="u.status === 'ACTIVE'"
                            [class.border-green-200]="u.status !== 'ACTIVE'"
                            [class.text-green-600]="u.status !== 'ACTIVE'"
                            [class.hover:bg-green-50]="u.status !== 'ACTIVE'">
                      {{ u.status === 'ACTIVE' ? 'Khoá' : 'Mở khoá' }}
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>

          <!-- Load more -->
          @if (hasMore()) {
            <div class="p-4 border-t border-gray-100 text-center">
              <button (click)="loadMore()"
                      class="px-5 py-1.5 text-sm border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 transition-colors">
                Xem thêm
              </button>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class AdminUsersComponent implements OnInit {
  private adminSvc = inject(AdminService);
  private toast    = inject(ToastService);

  readonly usersIcon = Users;

  roleTabs = [
    { value: '',           label: 'Tất cả' },
    { value: 'CLIENT',     label: 'Client' },
    { value: 'FREELANCER', label: 'Freelancer' },
    { value: 'ADMIN',      label: 'Admin' },
  ];

  users      = signal<AdminUser[]>([]);
  loading    = signal(true);
  activeRole = signal('');
  page       = 0;
  hasMore    = signal(false);

  ngOnInit() { this.load(true); }

  setRole(role: string) {
    this.activeRole.set(role);
    this.load(true);
  }

  load(reset = false) {
    if (reset) { this.page = 0; this.users.set([]); }
    this.loading.set(true);
    const role = this.activeRole() || undefined;
    this.adminSvc.getUsers(role, this.page, 20).subscribe({
      next: p => {
        this.users.update(list => [...list, ...p.content]);
        this.hasMore.set(p.number + 1 < p.totalPages);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  loadMore() {
    this.page++;
    this.load(false);
  }

  toggleStatus(u: AdminUser) {
    const newStatus = u.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE';
    this.adminSvc.updateUserStatus(u.id, newStatus).subscribe({
      next: () => {
        u.status = newStatus;
        this.toast.success(`Đã ${newStatus === 'ACTIVE' ? 'mở khoá' : 'khoá'} tài khoản`);
        this.users.update(list => [...list]); // trigger signal
      },
      error: err => this.toast.error(err?.message || 'Thao tác thất bại')
    });
  }

  roleClass(role: string): string {
    return {
      CLIENT:     'bg-blue-100 text-blue-700',
      FREELANCER: 'bg-indigo-100 text-indigo-700',
      ADMIN:      'bg-red-100 text-red-700'
    }[role] ?? 'bg-gray-100 text-gray-600';
  }

  statusLabel(s: string): string {
    return { ACTIVE: 'Hoạt động', SUSPENDED: 'Bị khoá', INACTIVE: 'Chưa kích hoạt' }[s] ?? s;
  }

  statusClass(s: string): string {
    return {
      ACTIVE:    'bg-green-100 text-green-700',
      SUSPENDED: 'bg-red-100 text-red-600',
      INACTIVE:  'bg-gray-100 text-gray-400'
    }[s] ?? '';
  }
}
