import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="bg-gray-900 text-gray-400 mt-auto">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- Brand -->
          <div class="md:col-span-1">
            <div class="flex items-center gap-2 font-bold text-xl text-white mb-3">
              <svg class="h-6 w-6 text-indigo-400" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
              TopFreelancer
            </div>
            <p class="text-sm leading-relaxed">
              Nền tảng kết nối freelancer và doanh nghiệp hàng đầu Việt Nam.
            </p>
          </div>

          <!-- Links -->
          <div>
            <h4 class="text-white font-semibold text-sm mb-3">Dành cho Freelancer</h4>
            <ul class="space-y-2 text-sm">
              <li><a routerLink="/jobs" class="hover:text-white transition-colors">Tìm việc làm</a></li>
              <li><a routerLink="/register" class="hover:text-white transition-colors">Đăng ký</a></li>
              <li><a routerLink="/freelancer/jobs" class="hover:text-white transition-colors">Quản lý công việc</a></li>
            </ul>
          </div>

          <div>
            <h4 class="text-white font-semibold text-sm mb-3">Dành cho Client</h4>
            <ul class="space-y-2 text-sm">
              <li><a routerLink="/client/jobs/new" class="hover:text-white transition-colors">Đăng dự án</a></li>
              <li><a routerLink="/client/jobs" class="hover:text-white transition-colors">Quản lý dự án</a></li>
              <li><a routerLink="/register" class="hover:text-white transition-colors">Đăng ký</a></li>
            </ul>
          </div>

          <div>
            <h4 class="text-white font-semibold text-sm mb-3">Hỗ trợ</h4>
            <ul class="space-y-2 text-sm">
              <li><span class="hover:text-white transition-colors cursor-default">Trung tâm hỗ trợ</span></li>
              <li><span class="hover:text-white transition-colors cursor-default">Điều khoản sử dụng</span></li>
              <li><span class="hover:text-white transition-colors cursor-default">Chính sách bảo mật</span></li>
            </ul>
          </div>
        </div>

        <div class="border-t border-gray-800 mt-10 pt-6 text-sm text-center">
          © {{ year }} TopFreelancer. All rights reserved.
        </div>
      </div>
    </footer>
  `
})
export class FooterComponent {
  year = new Date().getFullYear();
}
