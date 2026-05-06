import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { ApplicationService } from '../../../core/services/application.service';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { AiService } from '../../../core/services/ai.service';
import { JobDetail } from '../../../core/models/job.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-apply-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, CurrencyVndPipe, LoadingSpinnerComponent],
  templateUrl: './apply-form.component.html'
})
export class ApplyFormComponent implements OnInit {
  private fb       = inject(FormBuilder);
  private route    = inject(ActivatedRoute);
  private router   = inject(Router);
  private jobSvc   = inject(JobService);
  private appSvc   = inject(ApplicationService);
  private fileSvc  = inject(FileUploadService);
  private toast    = inject(ToastService);
  private aiSvc    = inject(AiService);

  jobId     = 0;
  job       = signal<JobDetail | null>(null);
  loading   = signal(true);
  submitting = signal(false);
  uploading  = signal(false);
  suggestingCover = signal(false);
  attachmentUrl = signal<string | null>(null);
  attachmentName = signal<string | null>(null);

  form = this.fb.group({
    coverLetter:   ['', [Validators.required, Validators.minLength(20), Validators.maxLength(2000)]],
    proposedPrice: [null as number | null, [Validators.required, Validators.min(100000)]],
    estimatedDays: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  get f() { return this.form.controls; }
  get coverLen() { return this.form.value.coverLetter?.length ?? 0; }

  ngOnInit() {
    this.jobId = +this.route.snapshot.params['jobId'];
    this.jobSvc.getJob(this.jobId).subscribe({
      next: job => {
        this.job.set(job);
        this.loading.set(false);
        // Pre-fill suggested price midpoint
        if (job.budgetMin && job.budgetMax) {
          const mid = Math.round((job.budgetMin + job.budgetMax) / 2);
          this.f['proposedPrice'].setValue(mid);
        }
      },
      error: () => { this.loading.set(false); this.router.navigate(['/jobs']); }
    });
  }

  suggestCoverLetter() {
    const j = this.job();
    if (!j) return;
    this.suggestingCover.set(true);
    this.aiSvc.suggestCoverLetter(j.title, j.description ?? '', j.budgetMin ?? 0, j.budgetMax ?? 0).subscribe({
      next: text => {
        this.f['coverLetter'].setValue(text.substring(0, 2000));
        this.suggestingCover.set(false);
        this.toast.success('Đã tạo thư giới thiệu bằng AI');
      },
      error: () => { this.suggestingCover.set(false); this.toast.error('Không thể gọi AI, thử lại sau'); }
    });
  }

  onFileSelect(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.uploading.set(true);
    this.fileSvc.uploadFile(file, 'attachments').subscribe({
      next: res => {
        this.attachmentUrl.set(res.url);
        this.attachmentName.set(res.fileName);
        this.uploading.set(false);
      },
      error: () => { this.uploading.set(false); this.toast.error('Upload thất bại'); }
    });
  }

  removeAttachment() {
    this.attachmentUrl.set(null);
    this.attachmentName.set(null);
  }

  onSubmit() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.submitting()) return;
    this.submitting.set(true);
    const v = this.form.value;
    this.appSvc.apply(this.jobId, {
      coverLetter:   v.coverLetter!,
      proposedPrice: v.proposedPrice!,
      estimatedDays: v.estimatedDays!,
      attachmentUrl: this.attachmentUrl() ?? undefined
    }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.toast.success('Ứng tuyển thành công!');
        this.router.navigate(['/my/applications']);
      },
      error: err => {
        this.submitting.set(false);
        this.toast.error(err?.error?.message || 'Không thể gửi ứng tuyển');
      }
    });
  }
}
