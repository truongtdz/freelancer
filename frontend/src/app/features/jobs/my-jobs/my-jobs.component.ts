import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { JobListItem, JobStatus } from '../../../core/models/job.model';
import { PageResponse } from '../../../core/models/api.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

type TabStatus = '' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

@Component({
  selector: 'app-my-jobs',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyVndPipe, TimeAgoPipe,
            PaginationComponent, EmptyStateComponent, LoadingSpinnerComponent, ConfirmDialogComponent],
  templateUrl: './my-jobs.component.html'
})
export class MyJobsComponent implements OnInit {
  private jobService = inject(JobService);
  private toast = inject(ToastService);

  activeTab = signal<TabStatus>('');
  jobs = signal<PageResponse<JobListItem> | null>(null);
  loading = signal(false);
  page = signal(0);
  closeTarget = signal<JobListItem | null>(null);

  tabs: { value: TabStatus; label: string }[] = [
    { value: '', label: 'Tất cả' },
    { value: 'OPEN', label: 'Đang mở' },
    { value: 'IN_PROGRESS', label: 'Đang thực hiện' },
    { value: 'COMPLETED', label: 'Hoàn thành' },
    { value: 'CANCELLED', label: 'Đã đóng' }
  ];

  ngOnInit() { this.loadJobs(); }

  setTab(tab: TabStatus) { this.activeTab.set(tab); this.page.set(0); this.loadJobs(); }

  loadJobs() {
    this.loading.set(true);
    const status = this.activeTab() || undefined;
    this.jobService.getMyJobs({ status: status as JobStatus, page: this.page(), size: 10 }).subscribe({
      next: data => { this.jobs.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onPageChange(p: number) { this.page.set(p); this.loadJobs(); }

  onCloseJob(confirmed: boolean) {
    const job = this.closeTarget();
    this.closeTarget.set(null);
    if (!confirmed || !job) return;
    this.jobService.closeJob(job.id).subscribe({
      next: () => { this.toast.success('Đã đóng job'); this.loadJobs(); },
      error: err => this.toast.error(err?.message || 'Không thể đóng job')
    });
  }

  statusLabel(s: string) {
    return ({OPEN:'Đang mở',IN_PROGRESS:'Đang thực hiện',
             COMPLETED:'Hoàn thành',CANCELLED:'Đã đóng'} as any)[s] ?? s;
  }
  statusClass(s: string) {
    return ({OPEN:'bg-green-100 text-green-700',IN_PROGRESS:'bg-blue-100 text-blue-700',
             COMPLETED:'bg-gray-100 text-gray-600',CANCELLED:'bg-red-100 text-red-600'} as any)[s] ?? '';
  }
}
