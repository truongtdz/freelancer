import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators, AbstractControl, ValidationErrors, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { UserRole } from '../../../core/models/user.model';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pwd = control.get('password')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return pwd && confirm && pwd !== confirm ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  loading = signal(false);
  showPassword = signal(false);
  step = signal<1 | 2>(1); // step 1: choose role, step 2: fill form

  selectedRole = signal<UserRole | null>(null);
  readonly CLIENT: UserRole = 'CLIENT';
  readonly FREELANCER: UserRole = 'FREELANCER';

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50),
                    Validators.pattern(/^[a-zA-Z0-9_]+$/)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.pattern(/^[0-9]{9,11}$/)]],
    password: ['', [Validators.required, Validators.minLength(8),
                    Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/)]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordMatchValidator });

  get fullName() { return this.form.get('fullName')!; }
  get username() { return this.form.get('username')!; }
  get email() { return this.form.get('email')!; }
  get phone() { return this.form.get('phone')!; }
  get password() { return this.form.get('password')!; }
  get confirmPassword() { return this.form.get('confirmPassword')!; }

  selectRole(role: UserRole) {
    this.selectedRole.set(role);
    this.step.set(2);
  }

  backToRole() {
    this.step.set(1);
  }

  togglePassword() { this.showPassword.update(v => !v); }

  submit() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading() || !this.selectedRole()) return;

    this.loading.set(true);
    const v = this.form.value;

    this.authService.register({
      fullName: v.fullName!,
      username: v.username!,
      email: v.email!,
      phone: v.phone || undefined,
      password: v.password!,
      role: this.selectedRole()! as 'CLIENT' | 'FREELANCER'
    }).subscribe({
      next: () => {
        this.toast.success('Đăng ký thành công! Chào mừng bạn đến TopFreelancer.');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.message || 'Đăng ký thất bại, vui lòng thử lại';
        this.toast.error(msg);
      }
    });
  }
}
