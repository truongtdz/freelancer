import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { PaymentInfo, PaymentInfoRequest } from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-payment-info',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LoadingSpinnerComponent],
  templateUrl: './payment-info.component.html'
})
export class PaymentInfoComponent implements OnInit {
  private userSvc  = inject(UserService);
  private fileSvc  = inject(FileUploadService);
  private toast    = inject(ToastService);

  paymentInfos  = signal<PaymentInfo[]>([]);
  loading       = signal(true);
  showForm      = signal(false);
  saving        = signal(false);
  uploadingQr   = signal(false);

  // Form fields
  bankName            = '';
  bankAccountNumber   = '';
  bankAccountHolder   = '';
  qrCodeUrl           = '';
  isDefault           = false;

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.userSvc.getMyPaymentInfos().subscribe({
      next: list => { this.paymentInfos.set(list); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openForm() {
    this.bankName          = '';
    this.bankAccountNumber = '';
    this.bankAccountHolder = '';
    this.qrCodeUrl         = '';
    this.isDefault         = false;
    this.showForm.set(true);
  }

  cancel() {
    this.showForm.set(false);
  }

  onQrFile(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.uploadingQr.set(true);
    this.fileSvc.uploadFile(file, 'qr-codes').subscribe({
      next: res => { this.qrCodeUrl = res.url; this.uploadingQr.set(false); },
      error: () => { this.uploadingQr.set(false); this.toast.error('Upload QR thất bại'); }
    });
  }

  save() {
    if (!this.bankName.trim() || !this.bankAccountNumber.trim() || !this.bankAccountHolder.trim()) {
      this.toast.error('Vui lòng điền đầy đủ thông tin ngân hàng');
      return;
    }
    this.saving.set(true);
    const req: PaymentInfoRequest = {
      bankName:          this.bankName.trim(),
      bankAccountNumber: this.bankAccountNumber.trim(),
      bankAccountHolder: this.bankAccountHolder.trim(),
      qrCodeUrl:         this.qrCodeUrl || undefined,
      isDefault:         this.isDefault
    };
    this.userSvc.savePaymentInfo(req).subscribe({
      next: () => {
        this.toast.success('Đã lưu tài khoản ngân hàng');
        this.saving.set(false);
        this.showForm.set(false);
        this.load();
      },
      error: err => {
        this.saving.set(false);
        this.toast.error(err?.message || 'Lưu thất bại');
      }
    });
  }

  delete(id: number) {
    if (!confirm('Xác nhận xoá tài khoản ngân hàng này?')) return;
    this.userSvc.deletePaymentInfo(id).subscribe({
      next: () => {
        this.toast.success('Đã xoá');
        this.paymentInfos.update(list => list.filter(p => p.id !== id));
      },
      error: err => this.toast.error(err?.message || 'Xoá thất bại')
    });
  }
}
