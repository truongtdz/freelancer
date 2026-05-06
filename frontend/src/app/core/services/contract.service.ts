import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ContractDetail, ContractListItem, ContractStatus } from '../models/contract.model';
import { PageResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class ContractService {
  private api = inject(ApiService);

  getMyContracts(page = 0, size = 10, status?: ContractStatus): Observable<PageResponse<ContractListItem>> {
    const params: Record<string, string | number> = { page, size };
    if (status) params['status'] = status;
    return this.api.get<PageResponse<ContractListItem>>('/contracts', params);
  }

  getContractDetail(id: number): Observable<ContractDetail> {
    return this.api.get<ContractDetail>(`/contracts/${id}`);
  }

  /** CLIENT: tạo link thanh toán VNPay */
  initiatePayment(contractId: number): Observable<{ paymentUrl: string }> {
    return this.api.post<{ paymentUrl: string }>(`/contracts/${contractId}/pay`);
  }

  /** FREELANCER: báo cáo tiến độ */
  createProgressReport(contractId: number, body: {
    title: string; content: string; progressPercentage: number; attachmentUrls?: string[];
  }): Observable<unknown> {
    return this.api.post(`/contracts/${contractId}/progress`, body);
  }

  /** FREELANCER: nộp bàn giao */
  submitCompletion(contractId: number, body: {
    summary: string; deliverableUrls: string[]; paymentInfoId?: number; qrCodeUrl?: string;
  }): Observable<ContractDetail> {
    return this.api.post<ContractDetail>(`/contracts/${contractId}/submit-completion`, body);
  }

  /** CLIENT: xác nhận hoàn thành */
  confirmCompletion(contractId: number): Observable<ContractDetail> {
    return this.api.post<ContractDetail>(`/contracts/${contractId}/confirm-completion`);
  }

  /** CLIENT: từ chối bàn giao */
  rejectCompletion(contractId: number, reason: string): Observable<ContractDetail> {
    return this.api.post<ContractDetail>(`/contracts/${contractId}/reject-completion`, { reason });
  }

  /** Mở tranh chấp */
  raiseDispute(contractId: number, body: {
    reason: string; description: string;
  }): Observable<ContractDetail> {
    return this.api.post<ContractDetail>(`/contracts/${contractId}/dispute`, body);
  }
}
