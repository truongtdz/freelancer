import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../core/models/api.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';

export interface ApplicationItem {
  id: number;
  freelancer: { id: number; fullName: string; avatarUrl?: string; username: string };
  coverLetter: string;
  proposedBudget: number;
  proposedDurationDays: number;
  status: string;
  createdAt: string;
}

@Component({
  selector: 'app-job-applications',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent, EmptyStateComponent,
            PaginationComponent, CurrencyVndPipe, TimeAgoPipe],
  template: `
    <div class="max-w-4xl mx-auto px-4 py-8">
      <div class="flex items-center gap-3 mb-6">
        <a [routerLink]="['/jobs', jobId]" class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500">
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </a>
        <h1 class="text-2xl font-bold text-gray-900">Ứng tuyển cho job</h1>
      </div>

      @if (loading()) {
        <div class="flex justify-center py-20"><app-loading-spinner size="lg" /></div>
      } @else if (!applications()?.content?.length) {
        <app-empty-state
          title="Chưa có ứng viên"
          description="Chưa có freelancer nào ứng tuyển vào job này" />
      } @else {
        <div class="space-y-4">
          @for (app of applications()!.content; track app.id) {
            <div class="bg-white rounded-xl border border-gray-200 p-5">
              <div class="flex items-start gap-4">
                <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0 overflow-hidden">
                  @if (app.freelancer.avatarUrl) {
                    <img [src]="app.freelancer.avatarUrl" [alt]="app.freelancer.fullName" class="w-full h-full object-cover" />
                  } @else {
                    <span class="text-indigo-600 font-semibold text-sm">{{ app.freelancer.fullName[0] }}</span>
                  }
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between gap-2 flex-wrap">
                    <a [routerLink]="['/profile', app.freelancer.id]"
                       class="font-semibold text-gray-900 hover:text-indigo-600">
                      {{ app.freelancer.fullName }}
                    </a>
                    <span [class]="'px-2 py-0.5 rounded-full text-xs font-medium ' + statusClass(app.status)">
                      {{ statusLabel(app.status) }}
                    </span>
                  </div>
                  <p class="text-xs text-gray-400 mt-0.5">{{ app.createdAt | timeAgo }}</p>
                  <p class="text-sm text-gray-600 mt-2 line-clamp-3">{{ app.coverLetter }}</p>
                  <div class="flex items-center gap-4 mt-3 text-sm text-gray-500">
                    <span>Đề xuất: <strong class="text-gray-800">{{ app.proposedBudget | currencyVnd }}</strong></span>
                    <span>·</span>
                    <span>Thời gian: <strong class="text-gray-800">{{ app.proposedDurationDays }} ngày</strong></span>
                  </div>
                </div>
              </div>
              @if (app.status === 'PENDING') {
                <div class="flex justify-end gap-2 mt-4 pt-4 border-t border-gray-100">
                  <button (click)="reject(app.id)"
                          class="px-3 py-1.5 text-xs font-medium border border-red-200 text-red-500 rounded-lg hover:bg-red-50">
                    Từ chối
                  </button>
                  <button (click)="accept(app.id)"
                          class="px-3 py-1.5 text-xs font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
                    Chấp nhận
                  </button>
                </div>
              }
            </div>
          }
        </div>

        <app-pagination
          [currentPage]="applications()!.number"
          [totalPages]="applications()!.totalPages"
          [totalElements]="applications()!.totalElements"
          [pageSize]="10"
          (pageChange)="onPageChange($event)" />
      }
    </div>
  `
})
export class JobApplicationsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  jobId = 0;
  loading = signal(false);
  page = signal(0);
  applications = signal<PageResponse<ApplicationItem> | null>(null);

  ngOnInit() {
    this.jobId = +this.route.snapshot.params['id'];
    this.load();
  }

  load() {
    this.loading.set(true);
    this.http.get<ApiResponse<PageResponse<ApplicationItem>>>(
      `${environment.apiUrl}/jobs/${this.jobId}/applications`,
      { params: { page: this.page(), size: 10 } }
    ).pipe(map(r => r.data)).subscribe({
      next: data => { this.applications.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onPageChange(p: number) { this.page.set(p); this.load(); }

  accept(appId: number) {
    this.http.put<ApiResponse<unknown>>(
      `${environment.apiUrl}/applications/${appId}/accept`, {}
    ).subscribe({ next: () => this.load(), error: () => {} });
  }

  reject(appId: number) {
    this.http.put<ApiResponse<unknown>>(
      `${environment.apiUrl}/applications/${appId}/reject`, {}
    ).subscribe({ next: () => this.load(), error: () => {} });
  }

  statusLabel(s: string) {
    return ({ PENDING: 'Chờ duyệt', ACCEPTED: 'Đã chấp nhận',
              REJECTED: 'Đã từ chối', WITHDRAWN: 'Đã rút' } as any)[s] ?? s;
  }
  statusClass(s: string) {
    return ({ PENDING: 'bg-yellow-100 text-yellow-700',
              ACCEPTED: 'bg-green-100 text-green-700',
              REJECTED: 'bg-red-100 text-red-600',
              WITHDRAWN: 'bg-gray-100 text-gray-500' } as any)[s] ?? '';
  }
}
