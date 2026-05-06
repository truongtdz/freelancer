import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { JobService } from '../../../core/services/job.service';
import { CategoryService } from '../../../core/services/category.service';
import { SkillService } from '../../../core/services/skill.service';
import { JobListItem, Category, Skill } from '../../../core/models/job.model';
import { PageResponse } from '../../../core/models/api.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    CurrencyVndPipe, TimeAgoPipe,
    PaginationComponent, EmptyStateComponent, LoadingSpinnerComponent
  ],
  templateUrl: './job-list.component.html'
})
export class JobListComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private jobService = inject(JobService);
  private categoryService = inject(CategoryService);
  private skillService = inject(SkillService);
  private destroy$ = new Subject<void>();

  categories = signal<Category[]>([]);
  allSkills = signal<Skill[]>([]);
  skillSearch = signal('');
  selectedSkillIds = signal<number[]>([]);
  jobs = signal<PageResponse<JobListItem> | null>(null);
  loading = signal(false);
  page = signal(0);

  filterForm = this.fb.group({
    keyword: [''],
    categoryId: [null as number | null],
    budgetType: ['' as string],
    workMode: ['' as string],
    budgetMin: [null as number | null],
    budgetMax: [null as number | null]
  });

  get filteredSkills(): Skill[] {
    const q = this.skillSearch().toLowerCase();
    return q ? this.allSkills().filter(s => s.name.toLowerCase().includes(q)) : this.allSkills();
  }

  isSkillSelected(id: number) { return this.selectedSkillIds().includes(id); }

  toggleSkill(id: number) {
    const cur = this.selectedSkillIds();
    this.selectedSkillIds.set(cur.includes(id) ? cur.filter(x => x !== id) : [...cur, id]);
    this.page.set(0); this.loadJobs();
  }

  getSkillName(id: number) { return this.allSkills().find(s => s.id === id)?.name ?? ''; }

  ngOnInit() {
    this.categoryService.getCategories().subscribe(c => this.categories.set(c));
    this.skillService.getSkills().subscribe(s => this.allSkills.set(s));
    this.loadJobs();

    this.filterForm.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      takeUntil(this.destroy$)
    ).subscribe(() => { this.page.set(0); this.loadJobs(); });
  }

  ngOnDestroy() { this.destroy$.next(); this.destroy$.complete(); }

  loadJobs() {
    this.loading.set(true);
    const f = this.filterForm.value;
    this.jobService.searchJobs({
      keyword: f.keyword || undefined,
      categoryId: f.categoryId || undefined,
      skillIds: this.selectedSkillIds().length ? this.selectedSkillIds() : undefined,
      budgetType: (f.budgetType || undefined) as any,
      workMode: (f.workMode || undefined) as any,
      budgetMin: f.budgetMin || undefined,
      budgetMax: f.budgetMax || undefined,
      page: this.page(), size: 12, sort: 'createdAt,desc'
    }).subscribe({
      next: data => { this.jobs.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onPageChange(p: number) { this.page.set(p); this.loadJobs(); }

  clearFilters() {
    this.filterForm.reset();
    this.selectedSkillIds.set([]);
    this.skillSearch.set('');
    this.page.set(0); this.loadJobs();
  }

  workModeLabel(w: string) {
    return ({ REMOTE: 'Từ xa', ONSITE: 'Tại chỗ', HYBRID: 'Kết hợp' } as any)[w] ?? w;
  }

  budgetTypeLabel(b: string) { return b === 'FIXED' ? 'Cố định' : 'Theo giờ'; }
}
