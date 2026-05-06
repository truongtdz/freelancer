import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getAccessToken();
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError(err => {
      if (err.status === 401) {
        inject(AuthService).logout();
      }
      return throwError(() => err);
    })
  );
};
