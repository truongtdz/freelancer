import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { firstValueFrom } from 'rxjs';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/auth/jwt.interceptor';
import { AuthService } from './core/auth/auth.service';

/** Block routing cho đến khi token được validate — tránh race condition redirect về /login */
function initAuth(auth: AuthService) {
  return () => firstValueFrom(auth.loadUser());
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    provideAnimations(),
    {
      provide: APP_INITIALIZER,
      useFactory: initAuth,
      deps: [AuthService],
      multi: true,
    },
  ],
};
