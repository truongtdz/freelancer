import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="flex flex-col items-center justify-center py-16 text-center">
      <svg class="h-16 w-16 text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0H4"/>
      </svg>
      <h3 class="text-lg font-semibold text-gray-700 mb-1">{{ title }}</h3>
      <p class="text-gray-500 text-sm mb-4">{{ description }}</p>
      @if (actionLabel && actionRoute) {
        <a [routerLink]="actionRoute"
           class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700">
          {{ actionLabel }}
        </a>
      }
    </div>
  `
})
export class EmptyStateComponent {
  @Input() title = 'Không có dữ liệu';
  @Input() description = '';
  @Input() actionLabel?: string;
  @Input() actionRoute?: string;
}
