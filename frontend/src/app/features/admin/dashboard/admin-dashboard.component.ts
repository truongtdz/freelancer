import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { DashboardStats } from '../../../core/models/admin.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import {
  LucideAngularModule,
  User, Briefcase, ClipboardList, Banknote, CreditCard, Scale
} from 'lucide-angular';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyVndPipe, LoadingSpinnerComponent, LucideAngularModule],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-20"><app-loading-spinner size="lg" /></div>
    } @else if (stats()) {
      <div>
        <h1 class="text-xl font-bold text-gray-900 mb-6">Trang quản trị</h1>

        <!-- Row 1: 4 stat cards -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">

          <!-- Total Users -->
          <div class="bg-white rounded-xl border border-gray-200 p-5">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-400 uppercase tracking-wide">Người dùng</span>
              <lucide-icon [img]="icons.User" [size]="22" class="text-indigo-400"></lucide-icon>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ stats()!.totalUsers }}</p>
            <p class="text-xs text-gray-400 mt-1">
              <span class="text-green-600 font-medium">{{ stats()!.activeUsers }}</span> đang hoạt động
            </p>
          </div>

          <!-- Total Jobs -->
          <div class="bg-white rounded-xl border border-gray-200 p-5">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-400 uppercase tracking-wide">Việc làm</span>
              <lucide-icon [img]="icons.Briefcase" [size]="22" class="text-blue-400"></lucide-icon>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ stats()!.totalJobs }}</p>
            <p class="text-xs text-gray-400 mt-1">
              <span class="text-blue-600 font-medium">{{ stats()!.openJobs }}</span> đang mở
            </p>
          </div>

          <!-- Contracts in progress -->
          <div class="bg-white rounded-xl border border-gray-200 p-5">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-400 uppercase tracking-wide">Hợp đồng</span>
              <lucide-icon [img]="icons.ClipboardList" [size]="22" class="text-purple-400"></lucide-icon>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ stats()!.contractsInProgress }}</p>
            <p class="text-xs text-gray-400 mt-1">
              / {{ stats()!.totalContracts }} tổng cộng
            </p>
          </div>

          <!-- Revenue -->
          <div class="bg-white rounded-xl border border-gray-200 p-5">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-semibold text-gray-400 uppercase tracking-wide">Doanh thu</span>
              <lucide-icon [img]="icons.Banknote" [size]="22" class="text-emerald-400"></lucide-icon>
            </div>
            <p class="text-lg font-bold text-emerald-600">{{ stats()!.totalRevenue | currencyVnd }}</p>
            <p class="text-xs text-gray-400 mt-1">
              Escrow: {{ stats()!.totalEscrow | currencyVnd }}
            </p>
          </div>
        </div>

        <!-- Row 2: Alert cards -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">

          <!-- Pending payouts -->
          <div class="bg-amber-50 border border-amber-200 rounded-xl p-5 flex items-center justify-between">
            <div>
              <p class="text-xs font-semibold text-amber-600 uppercase tracking-wide mb-1">Chờ thanh toán</p>
              <p class="text-3xl font-bold text-amber-700">{{ stats()!.pendingPayouts }}</p>
              <p class="text-xs text-amber-600 mt-1">hợp đồng cần xử lý</p>
            </div>
            <div class="text-center">
              <lucide-icon [img]="icons.CreditCard" [size]="32" class="text-amber-400 mb-2 mx-auto block"></lucide-icon>
              <a routerLink="/admin/payouts"
                 class="px-3 py-1.5 bg-amber-500 text-white text-xs font-semibold rounded-lg hover:bg-amber-600 transition-colors">
                Xử lý
              </a>
            </div>
          </div>

          <!-- Open disputes -->
          <div class="bg-red-50 border border-red-200 rounded-xl p-5 flex items-center justify-between">
            <div>
              <p class="text-xs font-semibold text-red-600 uppercase tracking-wide mb-1">Tranh chấp mở</p>
              <p class="text-3xl font-bold text-red-700">{{ stats()!.openDisputes }}</p>
              <p class="text-xs text-red-500 mt-1">đang chờ phân xử</p>
            </div>
            <div class="text-center">
              <lucide-icon [img]="icons.Scale" [size]="32" class="text-red-400 mb-2 mx-auto block"></lucide-icon>
              <a routerLink="/admin/disputes"
                 class="px-3 py-1.5 bg-red-500 text-white text-xs font-semibold rounded-lg hover:bg-red-600 transition-colors">
                Xem
              </a>
            </div>
          </div>
        </div>

        <!-- Row 3: Revenue chart (SVG sparkline) -->
        @if (stats()!.dailyRevenue && stats()!.dailyRevenue!.length > 0) {
          <div class="bg-white rounded-xl border border-gray-200 p-5">
            <h3 class="text-sm font-semibold text-gray-700 mb-4">Doanh thu 7 ngày gần nhất</h3>
            <div class="overflow-x-auto">
              <svg [attr.viewBox]="'0 0 700 120'" class="w-full h-32" preserveAspectRatio="none">
                <!-- Grid lines -->
                <line x1="0" y1="30" x2="700" y2="30" stroke="#f3f4f6" stroke-width="1"/>
                <line x1="0" y1="60" x2="700" y2="60" stroke="#f3f4f6" stroke-width="1"/>
                <line x1="0" y1="90" x2="700" y2="90" stroke="#f3f4f6" stroke-width="1"/>
                <!-- Line chart -->
                <polyline [attr.points]="chartPoints()"
                          fill="none" stroke="#6366f1" stroke-width="2.5"
                          stroke-linecap="round" stroke-linejoin="round"/>
                <!-- Area fill -->
                <polygon [attr.points]="chartArea()"
                         fill="#6366f1" fill-opacity="0.08"/>
                <!-- Data points -->
                @for (pt of chartData(); track $index) {
                  <circle [attr.cx]="pt.x" [attr.cy]="pt.y" r="4" fill="#6366f1"/>
                }
              </svg>
              <!-- X-axis labels -->
              <div class="flex justify-between mt-1">
                @for (d of stats()!.dailyRevenue!; track d.date) {
                  <span class="text-xs text-gray-400">{{ formatDay(d.date) }}</span>
                }
              </div>
            </div>
          </div>
        } @else {
          <div class="bg-white rounded-xl border border-gray-200 p-5">
            <h3 class="text-sm font-semibold text-gray-700 mb-4">Doanh thu 7 ngày gần nhất</h3>
            <div class="h-28 flex items-center justify-center text-gray-300">
              <p class="text-sm">Chưa có dữ liệu</p>
            </div>
          </div>
        }
      </div>
    }
  `
})
export class AdminDashboardComponent implements OnInit {
  private adminSvc = inject(AdminService);

  stats   = signal<DashboardStats | null>(null);
  loading = signal(true);

  readonly icons = { User, Briefcase, ClipboardList, Banknote, CreditCard, Scale };

  ngOnInit() {
    this.adminSvc.getDashboardStats().subscribe({
      next: s => { this.stats.set(s); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  chartData(): { x: number; y: number }[] {
    const data = this.stats()?.dailyRevenue ?? [];
    if (data.length === 0) return [];
    const max = Math.max(...data.map(d => d.amount), 1);
    const W = 700, H = 110, pad = 20;
    return data.map((d, i) => ({
      x: pad + (i / Math.max(data.length - 1, 1)) * (W - pad * 2),
      y: H - pad - ((d.amount / max) * (H - pad * 2))
    }));
  }

  chartPoints(): string {
    return this.chartData().map(p => `${p.x},${p.y}`).join(' ');
  }

  chartArea(): string {
    const pts = this.chartData();
    if (pts.length === 0) return '';
    const first = pts[0], last = pts[pts.length - 1];
    return `${first.x},110 ` + pts.map(p => `${p.x},${p.y}`).join(' ') + ` ${last.x},110`;
  }

  formatDay(dateStr: string): string {
    const d = new Date(dateStr);
    return `${d.getDate()}/${d.getMonth() + 1}`;
  }
}
