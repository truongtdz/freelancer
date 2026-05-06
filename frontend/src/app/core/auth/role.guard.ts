import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserRole } from '../models/user.model';

/** Factory: roleGuard(['CLIENT', 'ADMIN']) — includes auth check */
export function roleGuard(roles: UserRole[]): CanActivateFn {
  return (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLoggedIn()) {
      router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    if (auth.currentUser() && roles.includes(auth.currentUser()!.role)) return true;

    router.navigate(['/forbidden']);
    return false;
  };
}
