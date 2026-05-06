import { Injectable, inject, OnDestroy } from '@angular/core';
import { BehaviorSubject, interval, Subject, Subscription, switchMap, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService } from './api.service';
import { PageResponse } from '../models/api.model';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private api = inject(ApiService);

  readonly unreadCount$ = new BehaviorSubject<number>(0);
  readonly newNotification$ = new Subject<void>();   // emit khi có thông báo mới

  private pollSub?: Subscription;
  private prevCount = 0;

  getNotifications(page = 0, size = 20): import('rxjs').Observable<PageResponse<Notification>> {
    return this.api.get<PageResponse<Notification>>('/notifications', { page, size, sort: 'createdAt,desc' });
  }

  getUnreadCount(): import('rxjs').Observable<{ count: number }> {
    return this.api.get<{ count: number }>('/notifications/unread-count');
  }

  markRead(id: number): import('rxjs').Observable<void> {
    return this.api.put<void>(`/notifications/${id}/read`);
  }

  markAllRead(): import('rxjs').Observable<void> {
    return this.api.put<void>('/notifications/read-all');
  }

  startPolling(): void {
    this.stopPolling();
    this.prevCount = 0;
    this.fetchUnreadCount(false);   // lần đầu không toast
    this.pollSub = interval(10_000).pipe(
      switchMap(() => this.getUnreadCount().pipe(catchError(() => EMPTY)))
    ).subscribe(r => this.applyCount(r.count, true));
  }

  stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = undefined;
  }

  refreshUnreadCount(): void {
    this.fetchUnreadCount(false);
  }

  private fetchUnreadCount(toast: boolean): void {
    this.getUnreadCount().pipe(catchError(() => EMPTY))
      .subscribe(r => this.applyCount(r.count, toast));
  }

  private applyCount(count: number, emitIfNew: boolean): void {
    if (emitIfNew && count > this.prevCount) {
      this.newNotification$.next();
    }
    this.prevCount = count;
    this.unreadCount$.next(count);
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }
}
