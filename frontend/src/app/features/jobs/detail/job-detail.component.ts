import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { JobDetail } from '../../../core/models/job.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyVndPipe, TimeAgoPipe,
            LoadingSpinnerComponent, ConfirmDialogComponent],
  templateUrl: './job-detail.component.html'
})
export class JobDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private jobService = inject(JobService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  job = signal<JobDetail | null>(null);
  loading = signal(true);
  actionLoading = signal(false);
  showCloseConfirm = signal(false);

  isLoggedIn = this.authService.isLoggedIn;
  currentUser = this.authService.currentUser;

  ngOnInit() {
    this.route.params.subscribe(p => {
      this.loading.set(true);
      this.jobService.getJob(+p['id']).subscribe({
        next: j => { this.job.set(j); this.loading.set(false); },
        error: () => { this.loading.set(false); this.router.navigate(['/jobs']); }
      });
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
  workModeLabel(w: string) {
    return ({REMOTE:'Từ xa',ONSITE:'Tại chỗ',HYBRID:'Kết hợp'} as any)[w] ?? w;
  }

  onCloseJob(confirmed: boolean) {
    this.showCloseConfirm.set(false);
    if (!confirmed) return;
    const j = this.job();
    if (!j) return;
    this.actionLoading.set(true);
    this.jobService.closeJob(j.id).subscribe({
      next: updated => {
        this.job.set(updated); this.actionLoading.set(false);
        this.toast.success('Đã đóng job');
      },
      error: err => { this.actionLoading.set(false); this.toast.error(err?.message || 'Không thể đóng job'); }
    });
  }

  get loginUrl() { return '/login?returnUrl=/jobs/' + this.job()?.id; }

  fileSize(bytes: number) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }
}
