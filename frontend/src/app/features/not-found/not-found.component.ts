import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="flex flex-col items-center justify-center min-h-[60vh] px-4 text-center">
      <div class="text-8xl font-bold text-indigo-100 mb-4 select-none">404</div>
      <h1 class="text-2xl font-bold text-gray-900 mb-2">Trang không tồn tại</h1>
      <p class="text-gray-500 mb-8 max-w-sm">
        Trang bạn đang tìm kiếm có thể đã bị xóa, đổi tên hoặc tạm thời không khả dụng.
      </p>
      <a routerLink="/"
         class="px-6 py-2.5 bg-indigo-600 text-white text-sm font-semibold rounded-lg hover:bg-indigo-700 transition-colors">
        Về trang chủ
      </a>
    </div>
  `
})
export class NotFoundComponent {}
