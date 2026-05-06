import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationService } from '../../../core/services/application.service';
import { JobService } from '../../../core/services/job.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { Application } from '../../../core/models/application.model';
import { PageResponse } from '../../../core/models/api.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

type SortMode = 'newest' | 'price_asc' | 'price_desc' | 'rating_desc';

@Component({
  selector: 'app-job-applications',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, CurrencyVndPipe, TimeAgoPipe,
            PaginationComponent, EmptyStateComponent, LoadingSpinnerComponent, ConfirmDialogComponent],
  templateUrl: './job-applications.component.html'
})
export class JobApplicationsComponent implements OnInit {
  private route  = inject(ActivatedRoute);
  private router = inject(Router);
  private appSvc = inject(ApplicationService);
  private jobSvc = inject(JobService);
  private toast  = inject(ToastService);

  jobId    = 0;
  jobTitle = signal('');
  data     = signal<PageResponse<Application> | null>(null);
  loading  = signal(false);
  page     = signal(0);
  sortMode = signal<SortMode>('newest');

  // Dialogs
  acceptTarget = signal<Application | null>(null);
  rejectTarget = signal<Application | null>(null);
  rejectReason = '';  // plain property — used with ngModel in template
  expandedIds  = signal<Set<number>>(new Set());

  // Sorted view (client-side)
  sortedApps = computed(() => {
    const apps = [...(this.data()?.content ?? [])];
    switch (this.sortMode()) {
      case 'price_asc':   return apps.sort((a, b) => a.proposedPrice - b.proposedPrice);
      case 'price_desc':  return apps.sort((a, b) => b.proposedPrice - a.proposedPrice);
      case 'rating_desc': return apps.sort((a, b) => (b.freelancerRating ?? 0) - (a.freelancerRating ?? 0));
      default:            return apps.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }
  });

  sortOptions: { value: SortMode; label: string }[] = [
    { value: 'newest',     label: 'Mới nhất' },
    { value: 'price_asc',  label: 'Giá thấp nhất' },
    { value: 'price_desc', label: 'Giá cao nhất' },
    { value: 'rating_desc',label: 'Rating cao nhất' }
  ];

  ngOnInit() {
    this.jobId = +this.route.snapshot.params['id'];
    this.jobSvc.getJob(this.jobId).subscribe({
      next: job => this.jobTitle.set(job.title),
      error: () => this.router.navigate(['/my/jobs'])
    });
    this.load();
  }

  load() {
    this.loading.set(true);
    this.appSvc.getApplicationsByJob(this.jobId, this.page(), 20).subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  onPageChange(p: number) { this.page.set(p); this.load(); }

  toggleExpand(id: number) {
    const s = new Set(this.expandedIds());
    s.has(id) ? s.delete(id) : s.add(id);
    this.expandedIds.set(s);
  }
  isExpanded(id: number) { return this.expandedIds().has(id); }

  // Accept flow
  onAcceptConfirm(confirmed: boolean) {
    const app = this.acceptTarget();
    this.acceptTarget.set(null);
    if (!confirmed || !app) return;
    this.appSvc.acceptApplication(app.id).subscribe({
      next: contract => {
        this.toast.success('Đã chấp nhận. Hợp đồng PENDING_PAYMENT đã tạo.');
        this.router.navigate(['/contracts', contract.id]);
      },
      error: err => this.toast.error(err?.error?.message || 'Không thể chấp nhận')
    });
  }

  // Reject flow
  openRejectDialog(app: Application) {
    this.rejectTarget.set(app);
    this.rejectReason = '';
  }

  onRejectConfirm(confirmed: boolean) {
    const app = this.rejectTarget();
    this.rejectTarget.set(null);
    if (!confirmed || !app) return;
    this.appSvc.rejectApplication(app.id, this.rejectReason || undefined).subscribe({
      next: () => { this.toast.success('Đã từ chối ứng tuyển'); this.load(); },
      error: err => this.toast.error(err?.error?.message || 'Không thể từ chối')
    });
  }

  statusClass(s: string) {
    return ({
      PENDING:  'bg-yellow-100 text-yellow-700',
      ACCEPTED: 'bg-green-100 text-green-700',
      REJECTED: 'bg-red-100 text-red-600',
      WITHDRAWN:'bg-gray-100 text-gray-500'
    } as Record<string,string>)[s] ?? '';
  }

  statusLabel(s: string) {
    return ({ PENDING:'Chờ duyệt', ACCEPTED:'Đã chấp nhận',
              REJECTED:'Đã từ chối', WITHDRAWN:'Đã rút' } as Record<string,string>)[s] ?? s;
  }

  stars(rating: number = 0): boolean[] {
    return Array.from({ length: 5 }, (_, i) => i < Math.round(rating));
  }
}
