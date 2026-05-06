import { Component, OnInit, OnDestroy, inject, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ContractService } from '../../../core/services/contract.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { MessageService } from '../../../core/services/message.service';
import { ContractDetail, ContractStatus } from '../../../core/models/contract.model';
import { MessageResponse, ConversationResponse } from '../../../core/models/message.model';
import { CurrencyVndPipe } from '../../../shared/pipes/currency-vnd.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-contract-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, CurrencyVndPipe, TimeAgoPipe,
            LoadingSpinnerComponent, ConfirmDialogComponent],
  templateUrl: './contract-detail.component.html'
})
export class ContractDetailComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messageList') private messageList?: ElementRef<HTMLElement>;

  private route       = inject(ActivatedRoute);
  private router      = inject(Router);
  private contractSvc = inject(ContractService);
  private reviewSvc   = inject(ReviewService);
  private messageSvc  = inject(MessageService);
  private toast       = inject(ToastService);
  auth                = inject(AuthService);

  contract  = signal<ContractDetail | null>(null);
  loading   = signal(true);
  paying    = signal(false);

  // Confirm dialogs
  showConfirmDialog  = signal(false);
  showRejectDialog   = signal(false);
  showDisputeDialog  = signal(false);

  // Forms
  rejectReason      = '';
  disputeReason     = '';
  disputeDescription = '';

  // Tab navigation
  activeTab = signal<'overview'|'progress'|'completion'|'transactions'|'dispute'|'messages'>('overview');

  tabs = [
    { id: 'overview'      as const, label: 'Tổng quan' },
    { id: 'progress'      as const, label: 'Tiến độ' },
    { id: 'completion'    as const, label: 'Hoàn thành' },
    { id: 'transactions'  as const, label: 'Giao dịch' },
    { id: 'dispute'       as const, label: 'Tranh chấp' },
    { id: 'messages'      as const, label: 'Nhắn tin' },
  ];

  // Messaging
  conversation     = signal<ConversationResponse | null>(null);
  messages         = signal<MessageResponse[]>([]);
  messagesLoading  = signal(false);
  messageSending   = signal(false);
  newMessageText   = '';
  private shouldScrollToBottom = false;
  private pollSub?: Subscription;

  // Submit completion form
  showSubmitForm     = signal(false);
  submitSummary      = '';
  submitDeliverables = '';
  submitting         = signal(false);

  // Progress report form
  showProgressForm   = signal(false);
  progressTitle      = '';
  progressContent    = '';
  progressPct        = 50;
  reportingProgress  = signal(false);

  // Review form
  reviewRating       = 5;
  reviewComment      = '';
  submittingReview   = signal(false);
  reviewSubmitted    = signal(false);

  ngOnInit() {
    const id = +this.route.snapshot.params['id'];
    this.load(id);
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom && this.messageList) {
      const el = this.messageList.nativeElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  load(id: number = +this.route.snapshot.params['id']) {
    this.loading.set(true);
    this.contractSvc.getContractDetail(id).subscribe({
      next: c => { this.contract.set(c); this.loading.set(false); },
      error: () => { this.loading.set(false); this.router.navigate(['/my/contracts']); }
    });
  }

  // ── Tab switch ───────────────────────────────────────────────
  switchTab(tab: 'overview'|'progress'|'completion'|'transactions'|'dispute'|'messages') {
    this.activeTab.set(tab);
    if (tab === 'messages') this.loadMessages();
  }

  // ── Messaging ────────────────────────────────────────────────
  private loadMessages() {
    const c = this.contract();
    if (!c) return;
    if (this.conversation()) {
      this.fetchMessages(this.conversation()!.id);
      return;
    }
    this.messagesLoading.set(true);
    this.messageSvc.getOrCreateConversation(c.id).subscribe({
      next: conv => {
        this.conversation.set(conv);
        this.fetchMessages(conv.id);
        this.startMessagePolling(conv.id);
      },
      error: () => this.messagesLoading.set(false)
    });
  }

  private fetchMessages(conversationId: number) {
    this.messagesLoading.set(true);
    this.messageSvc.getMessages(conversationId).subscribe({
      next: page => {
        this.messages.set(page.content);
        this.messagesLoading.set(false);
        this.shouldScrollToBottom = true;
      },
      error: () => this.messagesLoading.set(false)
    });
  }

  private startMessagePolling(conversationId: number) {
    this.pollSub?.unsubscribe();
    this.pollSub = interval(8000).subscribe(() => {
      if (this.activeTab() === 'messages') this.fetchMessages(conversationId);
    });
  }

  sendMessage() {
    const text = this.newMessageText.trim();
    const conv = this.conversation();
    if (!text || !conv || this.messageSending()) return;
    this.messageSending.set(true);
    this.messageSvc.sendMessage(conv.id, text).subscribe({
      next: msg => {
        this.messages.update(list => [...list, msg]);
        this.newMessageText = '';
        this.messageSending.set(false);
        this.shouldScrollToBottom = true;
      },
      error: err => {
        this.messageSending.set(false);
        this.toast.error(err?.message || 'Không thể gửi tin nhắn');
      }
    });
  }

  onMessageKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  isMine(msg: MessageResponse): boolean {
    return msg.senderId === this.auth.currentUser()?.id;
  }

  // ── Payment ──────────────────────────────────────────────────
  pay() {
    const c = this.contract();
    if (!c) return;
    this.paying.set(true);
    this.contractSvc.initiatePayment(c.id).subscribe({
      next: ({ paymentUrl }) => { this.paying.set(false); window.location.href = paymentUrl; },
      error: err => { this.paying.set(false); this.toast.error(err?.message || 'Không thể tạo link thanh toán'); }
    });
  }

  // ── Progress report ──────────────────────────────────────────
  submitProgressReport() {
    const c = this.contract();
    if (!c || !this.progressTitle.trim() || !this.progressContent.trim()) return;
    this.reportingProgress.set(true);
    this.contractSvc.createProgressReport(c.id, {
      title: this.progressTitle.trim(),
      content: this.progressContent.trim(),
      progressPercentage: this.progressPct
    }).subscribe({
      next: () => {
        this.toast.success('Đã gửi báo cáo tiến độ');
        this.showProgressForm.set(false);
        this.progressTitle = ''; this.progressContent = ''; this.progressPct = 50;
        this.reportingProgress.set(false);
        this.load();
      },
      error: err => { this.reportingProgress.set(false); this.toast.error(err?.message || 'Lỗi'); }
    });
  }

  // ── Submit completion ─────────────────────────────────────────
  submitCompletion() {
    const c = this.contract();
    if (!c || !this.submitSummary.trim() || !this.submitDeliverables.trim()) return;
    const urls = this.submitDeliverables.split('\n').map(s => s.trim()).filter(Boolean);
    if (urls.length === 0) { this.toast.error('Cần ít nhất 1 link bàn giao'); return; }
    this.submitting.set(true);
    this.contractSvc.submitCompletion(c.id, { summary: this.submitSummary.trim(), deliverableUrls: urls })
      .subscribe({
        next: detail => {
          this.contract.set(detail);
          this.toast.success('Đã nộp bàn giao');
          this.showSubmitForm.set(false);
          this.submitSummary = ''; this.submitDeliverables = '';
          this.submitting.set(false);
        },
        error: err => { this.submitting.set(false); this.toast.error(err?.message || 'Không thể nộp bàn giao'); }
      });
  }

  // ── Confirm completion ────────────────────────────────────────
  onConfirmCompletion(confirmed: boolean) {
    this.showConfirmDialog.set(false);
    if (!confirmed) return;
    const c = this.contract()!;
    this.contractSvc.confirmCompletion(c.id).subscribe({
      next: detail => { this.contract.set(detail); this.toast.success('Đã xác nhận hoàn thành!'); },
      error: err => this.toast.error(err?.message || 'Không thể xác nhận')
    });
  }

  // ── Reject completion ─────────────────────────────────────────
  onRejectCompletion() {
    const c = this.contract();
    if (!c || !this.rejectReason.trim()) { this.toast.error('Vui lòng nhập lý do từ chối'); return; }
    this.contractSvc.rejectCompletion(c.id, this.rejectReason.trim()).subscribe({
      next: detail => {
        this.contract.set(detail);
        this.toast.success('Đã từ chối bàn giao');
        this.showRejectDialog.set(false);
        this.rejectReason = '';
      },
      error: err => this.toast.error(err?.message || 'Không thể từ chối')
    });
  }

  // ── Raise dispute ─────────────────────────────────────────────
  onRaiseDispute() {
    const c = this.contract();
    if (!c || !this.disputeReason.trim() || !this.disputeDescription.trim()) {
      this.toast.error('Vui lòng điền đầy đủ thông tin tranh chấp'); return;
    }
    this.contractSvc.raiseDispute(c.id, {
      reason: this.disputeReason.trim(),
      description: this.disputeDescription.trim()
    }).subscribe({
      next: detail => {
        this.contract.set(detail);
        this.toast.success('Đã mở tranh chấp');
        this.showDisputeDialog.set(false);
        this.disputeReason = ''; this.disputeDescription = '';
      },
      error: err => this.toast.error(err?.message || 'Không thể mở tranh chấp')
    });
  }

  // ── Review ───────────────────────────────────────────────────
  submitReview() {
    const c = this.contract();
    if (!c) return;
    this.submittingReview.set(true);
    this.reviewSvc.createReview(c.id, {
      rating:  this.reviewRating,
      comment: this.reviewComment.trim() || undefined
    }).subscribe({
      next: () => {
        this.toast.success('Đã gửi đánh giá!');
        this.reviewSubmitted.set(true);
        this.submittingReview.set(false);
      },
      error: err => {
        this.submittingReview.set(false);
        this.toast.error(err?.message || 'Không thể gửi đánh giá');
      }
    });
  }

  stars(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i + 1);
  }

  // ── Utils ─────────────────────────────────────────────────────
  private readonly STATUS_ORDER: ContractStatus[] = [
    'PENDING_PAYMENT', 'IN_PROGRESS', 'FREELANCER_SUBMITTED', 'CLIENT_CONFIRMED', 'PAID_OUT'
  ];

  isStatusPast(current: ContractStatus, step: string): boolean {
    const ci = this.STATUS_ORDER.indexOf(current as ContractStatus);
    const si = this.STATUS_ORDER.indexOf(step as ContractStatus);
    return ci > si && si >= 0;
  }

  statusLabel(s: ContractStatus): string {
    const map: Record<ContractStatus, string> = {
      PENDING_PAYMENT:      'Chờ thanh toán',
      IN_PROGRESS:          'Đang thực hiện',
      FREELANCER_SUBMITTED: 'Freelancer đã nộp',
      CLIENT_CONFIRMED:     'Client đã xác nhận',
      PAID_OUT:             'Đã thanh toán',
      DISPUTED:             'Đang tranh chấp',
      CANCELLED:            'Đã huỷ'
    };
    return map[s] ?? s;
  }

  statusClass(s: ContractStatus): string {
    const map: Partial<Record<ContractStatus, string>> = {
      PENDING_PAYMENT:      'bg-yellow-100 text-yellow-700',
      IN_PROGRESS:          'bg-blue-100 text-blue-700',
      FREELANCER_SUBMITTED: 'bg-purple-100 text-purple-700',
      CLIENT_CONFIRMED:     'bg-green-100 text-green-700',
      PAID_OUT:             'bg-emerald-100 text-emerald-700',
      DISPUTED:             'bg-red-100 text-red-600',
      CANCELLED:            'bg-gray-100 text-gray-500'
    };
    return map[s] ?? '';
  }

  submissionStatusLabel(s: string): string {
    const map: Record<string, string> = {
      PENDING_CONFIRM: 'Chờ xác nhận',
      CONFIRMED:       'Đã xác nhận',
      REJECTED:        'Đã từ chối',
      SUPERSEDED:      'Đã thay thế'
    };
    return map[s] ?? s;
  }
}
