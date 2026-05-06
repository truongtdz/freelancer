import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { Notification } from '../../../core/models/notification.model';
import {
  LucideAngularModule, LucideIconData,
  Bell, FileText, Sparkles, Banknote, ChartBar, CircleCheck, Landmark, MessageCircle
} from 'lucide-angular';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TimeAgoPipe, LoadingSpinnerComponent, LucideAngularModule],
  template: `
    <div class="max-w-3xl mx-auto px-4 py-8">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-xl font-bold text-gray-900">Thông báo</h1>
        @if (hasUnread()) {
          <button (click)="markAllRead()"
                  class="px-4 py-2 text-sm font-medium border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
            Đánh dấu tất cả đã đọc
          </button>
        }
      </div>

      <!-- Filter tabs -->
      <div class="flex gap-1 border-b border-gray-200 mb-5">
        @for (tab of [{id: 'all', label: 'Tất cả'}, {id: 'unread', label: 'Chưa đọc'}]; track tab.id) {
          <button (click)="setFilter($any(tab.id))"
                  class="px-4 py-2.5 text-sm font-medium border-b-2 -mb-px transition-colors"
                  [class.border-indigo-600]="filter() === tab.id"
                  [class.text-indigo-600]="filter() === tab.id"
                  [class.border-transparent]="filter() !== tab.id"
                  [class.text-gray-500]="filter() !== tab.id">
            {{ tab.label }}
          </button>
        }
      </div>

      @if (loading()) {
        <div class="flex justify-center py-20">
          <app-loading-spinner size="lg"/>
        </div>
      } @else if (!displayedNotifs().length) {
        <div class="text-center py-20 text-gray-400 flex flex-col items-center gap-2">
          <lucide-icon [img]="bellIcon" [size]="48" class="text-gray-300"></lucide-icon>
          <p class="text-sm font-medium text-gray-500">
            {{ filter() === 'unread' ? 'Không có thông báo chưa đọc' : 'Không có thông báo nào' }}
          </p>
        </div>
      } @else {
        <div class="space-y-2">
          @for (n of displayedNotifs(); track n.id) {
            <div (click)="clickNotif(n)"
                 class="flex items-start gap-4 p-4 rounded-xl border cursor-pointer transition-all hover:shadow-sm"
                 [class.bg-indigo-50]="!n.isRead"
                 [class.border-indigo-100]="!n.isRead"
                 [class.bg-white]="n.isRead"
                 [class.border-gray-200]="n.isRead">
              <lucide-icon [img]="notifIcon(n.type)" [size]="18" class="shrink-0 mt-0.5 text-indigo-500"></lucide-icon>
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-2">
                  <p class="text-sm font-semibold text-gray-900">{{ n.title }}</p>
                  <p class="text-xs text-gray-400 shrink-0">{{ n.createdAt | timeAgo }}</p>
                </div>
                <p class="text-sm text-gray-600 mt-0.5">{{ n.content }}</p>
              </div>
              @if (!n.isRead) {
                <span class="w-2.5 h-2.5 rounded-full bg-indigo-500 shrink-0 mt-1.5"></span>
              }
            </div>
          }
        </div>

        @if (hasMore()) {
          <div class="mt-5 text-center">
            <button (click)="loadMore()"
                    class="px-5 py-2.5 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50 transition-colors">
              Xem thêm
            </button>
          </div>
        }
      }
    </div>
  `
})
export class NotificationListComponent implements OnInit {
  private notifSvc  = inject(NotificationService);
  private authSvc   = inject(AuthService);
  private router    = inject(Router);
  private toast     = inject(ToastService);

  loading          = signal(true);
  allNotifs        = signal<Notification[]>([]);
  filter           = signal<'all' | 'unread'>('all');
  page             = 0;
  hasMore          = signal(false);

  displayedNotifs = () => {
    const notifs = this.allNotifs();
    return this.filter() === 'unread' ? notifs.filter(n => !n.isRead) : notifs;
  };

  hasUnread = () => this.allNotifs().some(n => !n.isRead);

  ngOnInit() { this.load(true); }

  setFilter(f: 'all' | 'unread') { this.filter.set(f); }

  load(reset = false) {
    if (reset) { this.page = 0; this.allNotifs.set([]); }
    this.loading.set(true);
    this.notifSvc.getNotifications(this.page, 20).subscribe({
      next: p => {
        this.allNotifs.update(list => [...list, ...p.content]);
        this.hasMore.set(p.number + 1 < p.totalPages);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  loadMore() {
    this.page++;
    this.load(false);
  }

  clickNotif(n: Notification) {
    if (!n.isRead) {
      this.notifSvc.markRead(n.id).subscribe(() => {
        this.allNotifs.update(list => list.map(item =>
          item.id === n.id ? { ...item, isRead: true } : item
        ));
        this.notifSvc.refreshUnreadCount();
      });
    }
    this.navigateByRef(n.referenceType, n.referenceId);
  }

  markAllRead() {
    this.notifSvc.markAllRead().subscribe({
      next: () => {
        this.allNotifs.update(list => list.map(n => ({ ...n, isRead: true })));
        this.notifSvc.refreshUnreadCount();
        this.toast.success('Đã đánh dấu tất cả đã đọc');
      },
      error: () => this.toast.error('Có lỗi xảy ra')
    });
  }

  private navigateByRef(refType?: string, refId?: number) {
    if (!refType || !refId) return;
    const role = this.authSvc.currentUser()?.role;
    switch (refType) {
      case 'Contract':
        this.router.navigate(['/contracts', refId]); break;
      case 'Application':
        if (role === 'FREELANCER') this.router.navigate(['/my/applications']);
        break;
      case 'Job':
        this.router.navigate(['/jobs', refId]); break;
      case 'Payout':
        this.router.navigate(['/my/contracts']); break;
    }
  }

  readonly bellIcon = Bell;

  notifIcon(type: string): LucideIconData {
    const map: Record<string, LucideIconData> = {
      NEW_APPLICATION:      FileText,
      APPLICATION_ACCEPTED: Sparkles,
      PAYMENT_RECEIVED:     Banknote,
      PROGRESS_REPORT:      ChartBar,
      JOB_COMPLETED:        CircleCheck,
      PAYOUT_COMPLETED:     Landmark,
      NEW_MESSAGE:          MessageCircle,
      SYSTEM:               Bell,
    };
    return map[type] ?? Bell;
  }
}
