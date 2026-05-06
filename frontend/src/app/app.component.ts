import { Component, computed, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { ToastComponent } from './shared/components/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, ToastComponent],
  template: `
    <app-toast />
    <div class="flex flex-col min-h-screen">
      @if (!isAdminRoute()) {
        <app-navbar />
      }
      <main class="flex-1">
        <router-outlet />
      </main>
      @if (!isAdminRoute()) {
        <app-footer />
      }
    </div>
  `
})
export class AppComponent {
  private router = inject(Router);
  private url = toSignal(this.router.events, { initialValue: null });
  isAdminRoute = computed(() => {
    this.url(); // trigger re-compute on navigation
    return this.router.url.startsWith('/admin');
  });
}
