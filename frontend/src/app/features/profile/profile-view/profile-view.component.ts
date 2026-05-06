import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserProfile } from '../../../core/models/user.model';
import { Review } from '../../../core/models/review.model';
import { PageResponse } from '../../../core/models/api.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-profile-view',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyVndPipe, TimeAgoPipe, LoadingSpinnerComponent],
  templateUrl: './profile-view.component.html'
})
export class ProfileViewComponent implements OnInit {
  private route      = inject(ActivatedRoute);
  private userSvc    = inject(UserService);
  private reviewSvc  = inject(ReviewService);
  auth               = inject(AuthService);

  profile     = signal<UserProfile | null>(null);
  reviews     = signal<PageResponse<Review> | null>(null);
  loading     = signal(true);
  activeTab   = signal<'about' | 'reviews' | 'contracts'>('about');

  isMe = computed(() => {
    const me = this.auth.currentUser();
    const p  = this.profile();
    return me && p ? me.id === p.id : false;
  });

  ngOnInit() {
    const paramId = this.route.snapshot.params['id'];
    const id = paramId === 'me' || !paramId
      ? (this.auth.currentUser()?.id ?? 0)
      : +paramId;
    this.userSvc.getProfile(id).subscribe({
      next: p => {
        this.profile.set(p);
        this.loading.set(false);
        // Load reviews
        this.reviewSvc.getReviewsByUser(id, 0, 10).subscribe({
          next: r => this.reviews.set(r),
          error: () => {}
        });
      },
      error: () => this.loading.set(false)
    });
  }

  loadMoreReviews() {
    const p = this.profile();
    const r = this.reviews();
    if (!p || !r) return;
    const nextPage = r.number + 1;
    this.reviewSvc.getReviewsByUser(p.id, nextPage, 10).subscribe({
      next: more => this.reviews.update(prev => prev ? {
        ...more,
        content: [...prev.content, ...more.content]
      } : more)
    });
  }

  stars(rating: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
  }

  skillLevelClass(level: string): string {
    return {
      BEGINNER:     'bg-gray-100 text-gray-600',
      INTERMEDIATE: 'bg-blue-100 text-blue-700',
      EXPERT:       'bg-indigo-100 text-indigo-700'
    }[level] ?? 'bg-gray-100 text-gray-600';
  }

  skillLevelLabel(level: string): string {
    return { BEGINNER: 'Cơ bản', INTERMEDIATE: 'Trung cấp', EXPERT: 'Chuyên gia' }[level] ?? level;
  }

  roleLabel(role: string): string {
    return { CLIENT: 'Client', FREELANCER: 'Freelancer', ADMIN: 'Admin' }[role] ?? role;
  }

  roleClass(role: string): string {
    return {
      CLIENT:     'bg-blue-100 text-blue-700',
      FREELANCER: 'bg-indigo-100 text-indigo-700',
      ADMIN:      'bg-red-100 text-red-700'
    }[role] ?? 'bg-gray-100 text-gray-600';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN');
  }

  avgRating(): number {
    return this.profile()?.ratingAvg ?? 0;
  }

  setTab(id: string): void {
    this.activeTab.set(id as 'about' | 'reviews' | 'contracts');
  }
}
