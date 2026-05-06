import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  toasts = signal<Toast[]>([]);

  success(message: string, duration = 3000) {
    this.add(message, 'success', duration);
  }

  error(message: string, duration = 5000) {
    this.add(message, 'error', duration);
  }

  info(message: string, duration = 3000) {
    this.add(message, 'info', duration);
  }

  dismiss(id: number) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }

  private add(message: string, type: ToastType, duration: number) {
    const id = ++this.counter;
    this.toasts.update(list => [...list, { id, message, type, duration }]);
    setTimeout(() => this.dismiss(id), duration);
  }
}
