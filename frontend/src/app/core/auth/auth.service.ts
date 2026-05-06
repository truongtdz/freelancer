import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { ApiService } from '../services/api.service';
import { AuthResponse, LoginRequest, RegisterRequest, User, UserRole } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'accessToken';
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  currentUser = signal<User | null>(null);
  isLoggedIn = computed(() => this.currentUser() !== null);

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/login', req).pipe(
      tap(res => {
        localStorage.setItem(this.TOKEN_KEY, res.accessToken);
        this.currentUser.set(res.user);
      })
    );
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/auth/register', req).pipe(
      tap(res => {
        localStorage.setItem(this.TOKEN_KEY, res.accessToken);
        this.currentUser.set(res.user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /** Gọi khi khởi động app — trả về Observable để APP_INITIALIZER await.
   *  Không navigate khi token hết hạn (để router tự xử lý sau khi init xong). */
  loadUser(): Observable<void> {
    const token = this.getAccessToken();
    if (!token) return of(undefined);
    return this.api.get<User>('/users/me').pipe(
      tap(user => this.currentUser.set(user)),
      map(() => undefined),
      catchError(() => {
        // Token không hợp lệ — xoá token, không navigate (router sẽ xử lý)
        localStorage.removeItem(this.TOKEN_KEY);
        this.currentUser.set(null);
        return of(undefined);
      })
    );
  }

  isRole(role: UserRole): boolean {
    return this.currentUser()?.role === role;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    const role = this.currentUser()?.role;
    return role ? roles.includes(role) : false;
  }
}
