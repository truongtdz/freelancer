import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { CategoryService } from '../../../core/services/category.service';
import { SkillService } from '../../../core/services/skill.service';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { Category, Skill, UploadResponse, JobDetail } from '../../../core/models/job.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

function budgetMaxValidator(group: AbstractControl) {
  const min = group.get('budgetMin')?.value;
  const max = group.get('budgetMax')?.value;
  if (min && max && +max < +min) return { budgetMaxLow: true };
  return null;
}

@Component({
  selector: 'app-job-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoadingSpinnerComponent],
  templateUrl: './job-edit.component.html'
})
export class JobEditComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private jobService = inject(JobService);
  private categoryService = inject(CategoryService);
  private skillService = inject(SkillService);
  private fileUploadService = inject(FileUploadService);
  private toast = inject(ToastService);

  jobId = signal(0);
  pageLoading = signal(true);
  categories = signal<Category[]>([]);
  allSkills = signal<Skill[]>([]);
  skillSearch = signal('');
  selectedSkillIds = signal<number[]>([]);
  uploadedFiles = signal<UploadResponse[]>([]);
  submitting = signal(false);
  uploading = signal(false);

  jobForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', [Validators.required, Validators.maxLength(5000)]],
    categoryId: [null as number | null, Validators.required],
    budgetType: ['FIXED', Validators.required],
    budgetMin: [null as number | null, [Validators.required, Validators.min(100000)]],
    budgetMax: [null as number | null, [Validators.required, Validators.min(100000)]],
    workMode: ['REMOTE', Validators.required],
    location: [''],
    deadline: ['']
  }, { validators: budgetMaxValidator });

  get f() { return this.jobForm.controls; }
  get locationEnabled() { return ['ONSITE','HYBRID'].includes(this.jobForm.value.workMode ?? ''); }

  get filteredSkills(): Skill[] {
    const q = this.skillSearch().toLowerCase();
    return q ? this.allSkills().filter(s => s.name.toLowerCase().includes(q)) : this.allSkills();
  }

  isSkillSelected(id: number) { return this.selectedSkillIds().includes(id); }
  toggleSkill(id: number) {
    const cur = this.selectedSkillIds();
    this.selectedSkillIds.set(cur.includes(id) ? cur.filter(x => x !== id) : [...cur, id]);
  }
  getSkillName(id: number) { return this.allSkills().find(s => s.id === id)?.name ?? ''; }

  ngOnInit() {
    this.categoryService.getCategories().subscribe(c => this.categories.set(c));
    this.skillService.getSkills().subscribe(s => {
      this.allSkills.set(s);
      this.loadJob();
    });
  }

  private loadJob() {
    const id = +this.route.snapshot.params['id'];
    this.jobId.set(id);
    this.jobService.getJob(id).subscribe({
      next: (job: JobDetail) => {
        this.jobForm.patchValue({
          title: job.title,
          description: job.description,
          categoryId: job.category?.id ?? null,
          budgetType: job.budgetType,
          budgetMin: job.budgetMin,
          budgetMax: job.budgetMax,
          workMode: job.workMode,
          location: job.location ?? '',
          deadline: job.deadline ? job.deadline.substring(0, 10) : ''
        });
        this.selectedSkillIds.set(job.skills?.map(s => s.id) ?? []);
        this.uploadedFiles.set(job.attachments?.map(a => ({
          url: a.fileUrl, fileName: a.fileName,
          storedName: a.fileUrl.split('/').pop() ?? '',
          fileSize: a.fileSize, contentType: '', subFolder: 'attachments'
        })) ?? []);
        this.pageLoading.set(false);
      },
      error: () => { this.router.navigate(['/jobs']); }
    });
  }

  onFileSelect(event: Event) {
    const files = (event.target as HTMLInputElement).files;
    if (!files) return;
    Array.from(files).slice(0, 5 - this.uploadedFiles().length).forEach(file => {
      this.uploading.set(true);
      this.fileUploadService.uploadFile(file, 'attachments').subscribe({
        next: res => { this.uploadedFiles.update(f => [...f, res]); this.uploading.set(false); },
        error: () => { this.uploading.set(false); this.toast.error('Upload thất bại: ' + file.name); }
      });
    });
  }

  removeFile(storedName: string) {
    this.uploadedFiles.update(files => files.filter(x => x.storedName !== storedName));
  }

  onSubmit() {
    this.jobForm.markAllAsTouched();
    if (this.jobForm.invalid || this.selectedSkillIds().length === 0 || this.submitting()) return;

    this.submitting.set(true);
    const v = this.jobForm.value;
    this.jobService.updateJob(this.jobId(), {
      title: v.title!, description: v.description!,
      categoryId: v.categoryId!, budgetType: v.budgetType as any,
      budgetMin: v.budgetMin!, budgetMax: v.budgetMax!,
      workMode: v.workMode as any, location: v.location || undefined,
      deadline: v.deadline || undefined,
      skillIds: this.selectedSkillIds(),
      attachments: this.uploadedFiles().map(f => ({
        fileUrl: f.url, fileName: f.fileName, fileSize: f.fileSize
      }))
    }).subscribe({
      next: job => {
        this.submitting.set(false);
        this.toast.success('Cập nhật job thành công!');
        this.router.navigate(['/jobs', job.id]);
      },
      error: err => { this.submitting.set(false); this.toast.error(err?.message || 'Lỗi cập nhật'); }
    });
  }
}
