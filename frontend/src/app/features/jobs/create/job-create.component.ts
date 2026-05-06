import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators, AbstractControl, ReactiveFormsModule, FormArray } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { CategoryService } from '../../../core/services/category.service';
import { SkillService } from '../../../core/services/skill.service';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { AiService } from '../../../core/services/ai.service';
import { Category, Skill, UploadResponse } from '../../../core/models/job.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

function budgetMaxValidator(group: AbstractControl) {
  const min = group.get('budgetMin')?.value;
  const max = group.get('budgetMax')?.value;
  if (min && max && +max < +min) return { budgetMaxLow: true };
  return null;
}

@Component({
  selector: 'app-job-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoadingSpinnerComponent],
  templateUrl: './job-create.component.html'
})
export class JobCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  protected jobService = inject(JobService);
  private categoryService = inject(CategoryService);
  private skillService = inject(SkillService);
  private fileUploadService = inject(FileUploadService);
  protected toast = inject(ToastService);
  protected router = inject(Router);
  private aiSvc = inject(AiService);

  categories = signal<Category[]>([]);
  allSkills = signal<Skill[]>([]);
  skillSearch = signal('');
  selectedSkillIds = signal<number[]>([]);
  uploadedFiles = signal<UploadResponse[]>([]);
  submitting = signal(false);
  uploading = signal(false);
  suggestingDesc = signal(false);

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
    this.skillService.getSkills().subscribe(s => this.allSkills.set(s));
  }

  suggestDescription() {
    const title = this.jobForm.value.title?.trim();
    if (!title) { this.toast.error('Nhập tiêu đề trước khi gợi ý'); return; }
    const catId = this.jobForm.value.categoryId;
    const catName = catId ? (this.categories().find(c => c.id === catId)?.name ?? '') : '';
    this.suggestingDesc.set(true);
    this.aiSvc.suggestDescription(title, catName).subscribe({
      next: text => {
        this.jobForm.patchValue({ description: text });
        this.suggestingDesc.set(false);
        this.toast.success('Đã tạo mô tả bằng AI');
      },
      error: () => { this.suggestingDesc.set(false); this.toast.error('Không thể gọi AI, thử lại sau'); }
    });
  }

  onFileSelect(event: Event) {
    const files = (event.target as HTMLInputElement).files;
    if (!files) return;
    const max = 5;
    const arr = Array.from(files).slice(0, max - this.uploadedFiles().length);
    arr.forEach(file => {
      this.uploading.set(true);
      this.fileUploadService.uploadFile(file, 'attachments').subscribe({
        next: res => { this.uploadedFiles.update(f => [...f, res]); this.uploading.set(false); },
        error: err => { this.uploading.set(false); this.toast.error('Upload thất bại: ' + file.name); }
      });
    });
  }

  removeFile(storedName: string) {
    const f = this.uploadedFiles().find(x => x.storedName === storedName);
    if (f) {
      this.fileUploadService.deleteFile(f.subFolder, f.storedName).subscribe();
      this.uploadedFiles.update(files => files.filter(x => x.storedName !== storedName));
    }
  }

  onSubmit() {
    this.jobForm.markAllAsTouched();
    if (this.jobForm.invalid || this.selectedSkillIds().length === 0) return;
    if (this.submitting()) return;

    this.submitting.set(true);
    const v = this.jobForm.value;
    this.jobService.createJob({
      title: v.title!,
      description: v.description!,
      categoryId: v.categoryId!,
      budgetType: v.budgetType as any,
      budgetMin: v.budgetMin!,
      budgetMax: v.budgetMax!,
      workMode: v.workMode as any,
      location: v.location || undefined,
      deadline: v.deadline || undefined,
      skillIds: this.selectedSkillIds(),
      attachments: this.uploadedFiles().map(f => ({
        fileUrl: f.url, fileName: f.fileName, fileSize: f.fileSize
      }))
    }).subscribe({
      next: job => {
        this.submitting.set(false);
        this.toast.success('Đăng job thành công!');
        this.router.navigate(['/jobs', job.id]);
      },
      error: err => {
        this.submitting.set(false);
        this.toast.error(err?.message || 'Lỗi khi đăng job');
      }
    });
  }
}
