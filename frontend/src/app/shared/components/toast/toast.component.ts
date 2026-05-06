import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-80">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="flex items-start gap-3 px-4 py-3 rounded-lg shadow-lg text-white text-sm
                 animate-slide-in"
          [ngClass]="{
            'bg-green-500': toast.type === 'success',
            'bg-red-500':   toast.type === 'error',
            'bg-blue-500':  toast.type === 'info'
          }">
          <span class="flex-1">{{ toast.message }}</span>
          <button (click)="toastService.dismiss(toast.id)"
                  class="text-white/80 hover:text-white leading-none text-lg">&times;</button>
        </div>
      }
    </div>
  `,
  styles: [`
    @keyframes slide-in {
      from { opacity: 0; transform: translateX(100%); }
      to   { opacity: 1; transform: translateX(0); }
    }
    .animate-slide-in { animation: slide-in 0.25s ease-out; }
  `]
})
export class ToastComponent {
  readonly toastService = inject(ToastService);
}
