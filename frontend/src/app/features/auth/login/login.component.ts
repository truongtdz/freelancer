import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../shared/components/toast/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);

  loading = signal(false);
  showPassword = signal(false);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  get email() { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }

  togglePassword() { this.showPassword.update(v => !v); }

  submit() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    const { email, password } = this.form.value;

    this.authService.login({ email: email!, password: password! }).subscribe({
      next: (res) => {
        let returnUrl = '/jobs';
        if (res.user.role === 'ADMIN') {
          returnUrl = '/admin/dashboard';
        }
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.message || 'Email hoặc mật khẩu không đúng';
        this.toast.error(msg);
      }
    });
  }
}
