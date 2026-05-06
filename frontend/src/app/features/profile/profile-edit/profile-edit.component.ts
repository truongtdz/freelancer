import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { SkillService } from '../../../core/services/skill.service';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserProfile, UserSkill, SkillLevel } from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

interface SkillEntry { skillId: number; name: string; level: SkillLevel; }

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LoadingSpinnerComponent],
  templateUrl: './profile-edit.component.html'
})
export class ProfileEditComponent implements OnInit {
  private userSvc  = inject(UserService);
  private skillSvc = inject(SkillService);
  private fileSvc  = inject(FileUploadService);
  private toast    = inject(ToastService);
  private router   = inject(Router);
  auth             = inject(AuthService);

  profile   = signal<UserProfile | null>(null);
  loading   = signal(true);
  saving    = signal(false);

  // Form fields
  fullName       = '';
  phone          = '';
  avatarUrl      = '';
  bio            = '';
  title          = '';
  experienceYears: number | null = null;
  hourlyRate: number | null      = null;

  // Skills (FREELANCER)
  skills: SkillEntry[] = [];
  allSkills: { id: number; name: string; slug: string }[] = [];
  selectedSkillId: number | null = null;
  selectedLevel: SkillLevel = 'INTERMEDIATE';
  uploadingAvatar = signal(false);

  ngOnInit() {
    const me = this.auth.currentUser();
    if (!me) { this.router.navigate(['/login']); return; }

    // Load profile
    this.userSvc.getProfile(me.id).subscribe({
      next: p => {
        this.profile.set(p);
        this.loading.set(false);
        this.fullName        = p.fullName ?? '';
        this.phone           = p.phone ?? '';
        this.avatarUrl       = p.avatarUrl ?? '';
        this.bio             = p.bio ?? '';
        this.title           = p.title ?? '';
        this.experienceYears = p.experienceYears ?? null;
        this.hourlyRate      = p.hourlyRate ?? null;
        this.skills          = (p.skills ?? []).map(s => ({
          skillId: s.skillId, name: s.name, level: s.level
        }));
      },
      error: () => this.loading.set(false)
    });

    // Load all skills for freelancer
    if (me.role === 'FREELANCER') {
      this.skillSvc.getSkills().subscribe({
        next: list => this.allSkills = list.map(s => ({ id: s.id, name: s.name, slug: s.slug })),
        error: () => {}
      });
    }
  }

  onAvatarFile(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.uploadingAvatar.set(true);
    this.fileSvc.uploadFile(file, 'avatars').subscribe({
      next: res => { this.avatarUrl = res.url; this.uploadingAvatar.set(false); },
      error: () => { this.uploadingAvatar.set(false); this.toast.error('Upload ảnh thất bại'); }
    });
  }

  addSkill() {
    if (!this.selectedSkillId) return;
    if (this.skills.find(s => s.skillId === this.selectedSkillId)) return;
    const skill = this.allSkills.find(s => s.id === this.selectedSkillId);
    if (!skill) return;
    this.skills = [...this.skills, {
      skillId: this.selectedSkillId,
      name: skill.name,
      level: this.selectedLevel
    }];
    this.selectedSkillId = null;
  }

  removeSkill(skillId: number) {
    this.skills = this.skills.filter(s => s.skillId !== skillId);
  }

  updateSkillLevel(skillId: number, level: SkillLevel) {
    this.skills = this.skills.map(s => s.skillId === skillId ? { ...s, level } : s);
  }

  save() {
    this.saving.set(true);
    const me = this.auth.currentUser();
    const req = {
      fullName:       this.fullName.trim() || undefined,
      phone:          this.phone.trim() || undefined,
      avatarUrl:      this.avatarUrl || undefined,
      bio:            this.bio.trim() || undefined,
      title:          this.title.trim() || undefined,
      experienceYears: this.experienceYears ?? undefined,
      hourlyRate:     this.hourlyRate ?? undefined,
      skills: me?.role === 'FREELANCER'
        ? this.skills.map(s => ({ skillId: s.skillId, level: s.level }))
        : undefined
    };
    this.userSvc.updateMyProfile(req).subscribe({
      next: p => {
        this.profile.set(p);
        this.saving.set(false);
        this.toast.success('Đã lưu hồ sơ');
        this.router.navigate(['/profile/me']);
      },
      error: err => {
        this.saving.set(false);
        this.toast.error(err?.message || 'Lưu thất bại');
      }
    });
  }

  skillLevelOptions: { value: SkillLevel; label: string }[] = [
    { value: 'BEGINNER',     label: 'Cơ bản' },
    { value: 'INTERMEDIATE', label: 'Trung cấp' },
    { value: 'EXPERT',       label: 'Chuyên gia' },
  ];

  availableSkills() {
    const usedIds = new Set(this.skills.map(s => s.skillId));
    return this.allSkills.filter(s => !usedIds.has(s.id));
  }
}
