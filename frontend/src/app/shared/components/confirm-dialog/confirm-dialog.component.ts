import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ title }}</h3>
        <p class="text-gray-600 text-sm mb-6">{{ message }}</p>
        <div class="flex justify-end gap-3">
          <button (click)="confirmed.emit(false)"
                  class="px-4 py-2 rounded-lg border border-gray-300 text-sm text-gray-700 hover:bg-gray-50">
            {{ cancelLabel }}
          </button>
          <button (click)="confirmed.emit(true)"
                  class="px-4 py-2 rounded-lg text-sm text-white"
                  [class.bg-red-600]="danger"
                  [class.hover:bg-red-700]="danger"
                  [class.bg-indigo-600]="!danger"
                  [class.hover:bg-indigo-700]="!danger">
            {{ confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  `
})
export class ConfirmDialogComponent {
  @Input() title = 'Xác nhận';
  @Input() message = 'Bạn có chắc chắn không?';
  @Input() confirmLabel = 'Xác nhận';
  @Input() cancelLabel = 'Hủy';
  @Input() danger = false;
  @Output() confirmed = new EventEmitter<boolean>();
}
