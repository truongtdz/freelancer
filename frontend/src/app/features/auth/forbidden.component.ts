import { Component } from '@angular/core';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  template: `<div class="p-8 text-center text-red-500 text-2xl">403 — Không có quyền truy cập</div>`,
})
export class ForbiddenComponent {}
