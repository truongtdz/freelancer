import { Component, inject, signal, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth/auth.service';
import {
  LucideAngularModule, LucideIconData,
  LayoutDashboard, Users, ClipboardList, CreditCard, Scale, Settings, LogOut, ChevronDown
} from 'lucide-angular';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, LucideAngularModule],
  template: `
    <div class="flex h-screen bg-gray-50">

      <!-- Sidebar -->
      <aside class="w-60 bg-gray-900 flex flex-col shrink-0">
        <!-- Logo -->
        <div class="h-14 flex items-center px-5 border-b border-gray-700 gap-2 shrink-0">
          <lucide-icon [img]="icons.Settings" [size]="18" class="text-indigo-400"></lucide-icon>
          <span class="text-white font-bold text-base">Admin Panel</span>
        </div>

        <!-- Nav items -->
        <nav class="flex-1 py-3 space-y-0.5 overflow-y-auto">
          @for (item of navItems; track item.path) {
            <a [routerLink]="item.path" routerLinkActive="bg-gray-700 text-white"
               [routerLinkActiveOptions]="{ exact: item.exact }"
               class="flex items-center gap-3 px-5 py-2.5 text-gray-300 hover:bg-gray-800 hover:text-white transition-colors text-sm">
              <lucide-icon [img]="item.icon" [size]="16" class="shrink-0"></lucide-icon>
              <span>{{ item.label }}</span>
            </a>
          }
        </nav>
      </aside>

      <!-- Main content -->
      <div class="flex-1 flex flex-col min-w-0">

        <!-- Topbar -->
        <header class="h-14 bg-white border-b border-gray-200 flex items-center justify-end px-6 shrink-0">
          <!-- User button + dropdown -->
          <div class="relative">
            <button id="admin-user-btn" (click)="toggleMenu()"
                    class="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-gray-100 transition-colors">
              <div class="w-7 h-7 rounded-full bg-indigo-600 flex items-center justify-center text-white text-xs font-bold shrink-0">
                {{ initials() }}
              </div>
              <span class="text-sm font-medium text-gray-700">{{ auth.currentUser()?.fullName ?? 'Admin' }}</span>
              <lucide-icon [img]="icons.ChevronDown" [size]="14" class="text-gray-400"
                           [class.rotate-180]="menuOpen()"></lucide-icon>
            </button>

            @if (menuOpen()) {
              <div id="admin-user-dropdown"
                   class="absolute right-0 top-full mt-1.5 w-44 bg-white rounded-xl border border-gray-200 shadow-lg overflow-hidden z-50">
                <div class="px-4 py-2.5 border-b border-gray-100">
                  <p class="text-xs font-semibold text-gray-800 truncate">{{ auth.currentUser()?.fullName }}</p>
                  <p class="text-[11px] text-gray-400 truncate">{{ auth.currentUser()?.email }}</p>
                </div>
                <button (click)="logout()"
                        class="w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-red-500 hover:bg-red-50 transition-colors">
                  <lucide-icon [img]="icons.LogOut" [size]="14" class="shrink-0"></lucide-icon>
                  Đăng xuất
                </button>
              </div>
            }
          </div>
        </header>

        <!-- Page content -->
        <main class="flex-1 overflow-y-auto p-6">
          <router-outlet />
        </main>
      </div>
    </div>
  `
})
export class AdminShellComponent {
  auth = inject(AuthService);
  private router = inject(Router);

  menuOpen = signal(false);

  readonly icons = { Settings, LogOut, ChevronDown };

  navItems: { path: string; label: string; icon: LucideIconData; exact: boolean }[] = [
    { path: '/admin',           label: 'Trang quản trị',  icon: LayoutDashboard, exact: true  },
    { path: '/admin/users',     label: 'Người dùng', icon: Users,           exact: false },
    { path: '/admin/contracts', label: 'Hợp đồng',   icon: ClipboardList,   exact: false },
    { path: '/admin/payouts',   label: 'Thanh toán',     icon: CreditCard,      exact: false },
    { path: '/admin/disputes',  label: 'Tranh chấp', icon: Scale,           exact: false },
    { path: '/admin/settings',  label: 'Cài đặt',    icon: Settings,        exact: false },
  ];

  initials(): string {
    return (this.auth.currentUser()?.fullName ?? 'A').charAt(0).toUpperCase();
  }

  toggleMenu() { this.menuOpen.update(v => !v); }

  logout() {
    this.menuOpen.set(false);
    this.auth.logout();
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent) {
    const t = e.target as HTMLElement;
    if (!t.closest('#admin-user-btn') && !t.closest('#admin-user-dropdown')) {
      this.menuOpen.set(false);
    }
  }
}
