import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (fullScreen) {
      <div class="fixed inset-0 flex items-center justify-center bg-white/70 z-50">
        <ng-container *ngTemplateOutlet="spinner" />
      </div>
    } @else {
      <ng-container *ngTemplateOutlet="spinner" />
    }

    <ng-template #spinner>
      <svg class="animate-spin text-indigo-600"
           [ngClass]="sizeClass"
           xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10"
                stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" fill="currentColor"
              d="M4 12a8 8 0 018-8v8H4z"/>
      </svg>
    </ng-template>
  `
})
export class LoadingSpinnerComponent {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() fullScreen = false;

  get sizeClass() {
    return { 'h-4 w-4': this.size === 'sm', 'h-8 w-8': this.size === 'md', 'h-12 w-12': this.size === 'lg' };
  }
}
