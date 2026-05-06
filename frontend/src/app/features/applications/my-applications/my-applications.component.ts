import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApplicationService } from '../../../core/services/application.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { Application, ApplicationStatus } from '../../../core/models/application.model';
import { PageResponse } from '../../../core/models/api.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

type TabStatus = '' | ApplicationStatus;

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyVndPipe, TimeAgoPipe,
            PaginationComponent, EmptyStateComponent, LoadingSpinnerComponent, ConfirmDialogComponent],
  templateUrl: './my-applications.component.html'
})
export class MyApplicationsComponent implements OnInit {
  private appSvc = inject(ApplicationService);
  private toast  = inject(ToastService);

  activeTab  = signal<TabStatus>('');
  apps       = signal<PageResponse<Application> | null>(null);
  loading    = signal(false);
  page       = signal(0);
  withdrawTarget = signal<Application | null>(null);

  tabs: { value: TabStatus; label: string }[] = [
    { value: '',          label: 'Tất cả' },
    { value: 'PENDING',   label: 'Chờ duyệt' },
    { value: 'ACCEPTED',  label: 'Đã chấp nhận' },
    { value: 'REJECTED',  label: 'Đã từ chối' },
    { value: 'WITHDRAWN', label: 'Đã rút' }
  ];

  ngOnInit() { this.load(); }

  setTab(tab: TabStatus) { this.activeTab.set(tab); this.page.set(0); this.load(); }
  onPageChange(p: number) { this.page.set(p); this.load(); }

  load() {
    this.loading.set(true);
    const status = this.activeTab() || undefined;
    this.appSvc.getMyApplications(status, this.page(), 10).subscribe({
      next: data => { this.apps.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onWithdraw(confirmed: boolean) {
    const app = this.withdrawTarget();
    this.withdrawTarget.set(null);
    if (!confirmed || !app) return;
    this.appSvc.withdrawApplication(app.id).subscribe({
      next: () => { this.toast.success('Đã rút ứng tuyển'); this.load(); },
      error: err => this.toast.error(err?.error?.message || 'Không thể rút ứng tuyển')
    });
  }

  statusLabel(s: ApplicationStatus) {
    return ({ PENDING:'Chờ duyệt', ACCEPTED:'Đã chấp nhận',
              REJECTED:'Đã từ chối', WITHDRAWN:'Đã rút' } as Record<string,string>)[s] ?? s;
  }

  statusClass(s: ApplicationStatus) {
    return ({
      PENDING:  'bg-yellow-100 text-yellow-700',
      ACCEPTED: 'bg-green-100 text-green-700',
      REJECTED: 'bg-red-100 text-red-600',
      WITHDRAWN:'bg-gray-100 text-gray-500'
    } as Record<string,string>)[s] ?? '';
  }
}
