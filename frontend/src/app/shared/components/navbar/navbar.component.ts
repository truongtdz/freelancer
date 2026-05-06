import { Component, inject, signal, HostListener, OnInit, OnDestroy, effect } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ToastService } from '../toast/toast.service';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';
import { UserRole } from '../../../core/models/user.model';
import { Notification } from '../../../core/models/notification.model';
import { Subscription } from 'rxjs';
import { catchError, EMPTY } from 'rxjs';
import {
  LucideAngularModule, LucideIconData,
  Bell, FileText, Sparkles, Banknote, ChartBar, CircleCheck, Landmark, MessageCircle
} from 'lucide-angular';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule, TimeAgoPipe, LucideAngularModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit, OnDestroy {
  private authService   = inject(AuthService);
  private router        = inject(Router);
  private notifSvc      = inject(NotificationService);
  private toast         = inject(ToastService);

  currentUser  = this.authService.currentUser;
  isLoggedIn   = this.authService.isLoggedIn;

  mobileOpen      = signal(false);
  dropdownOpen    = signal(false);
  bellOpen        = signal(false);
  unreadCount     = signal(0);
  recentNotifs    = signal<Notification[]>([]);
  loadingNotifs   = signal(false);

  readonly ADMIN:      UserRole = 'ADMIN';
  readonly CLIENT:     UserRole = 'CLIENT';
  readonly FREELANCER: UserRole = 'FREELANCER';

  private countSub?: Subscription;
  private newNotifSub?: Subscription;

  constructor() {
    effect(() => {
      const loggedIn = this.isLoggedIn();
      if (loggedIn) {
        this.notifSvc.startPolling();
        if (!this.countSub) {
          this.countSub = this.notifSvc.unreadCount$.subscribe(n => this.unreadCount.set(n));
        }
        if (!this.newNotifSub) {
          this.newNotifSub = this.notifSvc.newNotification$.subscribe(() => {
            this.toast.info('Bạn có thông báo mới');
          });
        }
      } else {
        this.notifSvc.stopPolling();
        this.unreadCount.set(0);
      }
    });
  }

  ngOnInit() {}

  ngOnDestroy() {
    this.notifSvc.stopPolling();
    this.countSub?.unsubscribe();
    this.newNotifSub?.unsubscribe();
  }

  toggleMobile()  { this.mobileOpen.update(v => !v); }
  toggleDropdown(){ this.dropdownOpen.update(v => !v); }
  closeDropdown() { this.dropdownOpen.set(false); }
  closeMobile()   { this.mobileOpen.set(false); }

  toggleBell() {
    const opening = !this.bellOpen();
    this.bellOpen.set(opening);
    this.dropdownOpen.set(false);
    if (opening) { this.loadRecentNotifs(); }
  }

  closeBell() { this.bellOpen.set(false); }

  loadRecentNotifs() {
    this.loadingNotifs.set(true);
    this.notifSvc.getNotifications(0, 5).pipe(catchError(() => EMPTY)).subscribe(p => {
      this.recentNotifs.set(p.content);
      this.loadingNotifs.set(false);
    });
  }

  clickNotif(n: Notification) {
    this.closeBell();
    if (!n.isRead) {
      this.notifSvc.markRead(n.id).subscribe(() => this.notifSvc.refreshUnreadCount());
    }
    this.navigateByRef(n.referenceType, n.referenceId);
  }

  private navigateByRef(refType?: string, refId?: number) {
    if (!refType || !refId) { this.router.navigate(['/notifications']); return; }
    const role = this.currentUser()?.role;
    switch (refType) {
      case 'Contract':
        this.router.navigate(['/contracts', refId]); break;
      case 'Application':
        if (role === 'FREELANCER') this.router.navigate(['/my/applications']);
        else this.router.navigate(['/notifications']);
        break;
      case 'Job':
        this.router.navigate(['/jobs', refId]); break;
      case 'Payout':
        this.router.navigate(['/my/contracts']); break;
      default:
        this.router.navigate(['/notifications']); break;
    }
  }

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

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent) {
    const target = e.target as HTMLElement;
    if (!target.closest('#user-menu-btn') && !target.closest('#user-dropdown')) {
      this.dropdownOpen.set(false);
    }
    if (!target.closest('#bell-btn') && !target.closest('#bell-dropdown')) {
      this.bellOpen.set(false);
    }
  }

  logout() {
    this.notifSvc.stopPolling();
    this.authService.logout();
    this.dropdownOpen.set(false);
    this.mobileOpen.set(false);
  }

  getDashboardRoute(): string {
    const role = this.currentUser()?.role;
    if (role === 'ADMIN') return '/admin/dashboard';
    if (role === 'CLIENT') return '/client/jobs';
    return '/freelancer/jobs';
  }

  getAvatarUrl(): string {
    return this.currentUser()?.avatarUrl || '';
  }

  getInitials(): string {
    const name = this.currentUser()?.fullName || this.currentUser()?.username || '?';
    return name.charAt(0).toUpperCase();
  }
}
