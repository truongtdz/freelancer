import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PageResponse } from '../models/api.model';
import {
  DashboardStats, PendingPayoutItem, PayoutCreateRequest, AdminPayout,
  AdminDispute, DisputeResolveRequest, DisputeStatus
} from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private api = inject(ApiService);

  getDashboardStats(): Observable<DashboardStats> {
    return this.api.get<DashboardStats>('/admin/dashboard/stats');
  }

  getPendingPayouts(page = 0, size = 20): Observable<PageResponse<PendingPayoutItem>> {
    return this.api.get<PageResponse<PendingPayoutItem>>('/admin/payouts/pending', { page, size });
  }

  payContract(contractId: number, req: PayoutCreateRequest): Observable<AdminPayout> {
    return this.api.post<AdminPayout>(`/admin/payouts/${contractId}/pay`, req);
  }

  getDisputes(status?: DisputeStatus, page = 0, size = 20): Observable<PageResponse<AdminDispute>> {
    const params: Record<string, string | number> = { page, size };
    if (status) params['status'] = status;
    return this.api.get<PageResponse<AdminDispute>>('/admin/disputes', params);
  }

  resolveDispute(id: number, req: DisputeResolveRequest): Observable<AdminDispute> {
    return this.api.put<AdminDispute>(`/admin/disputes/${id}/resolve`, req);
  }

  updateSetting(key: string, value: string): Observable<string> {
    return this.api.put<string>(`/admin/settings/${key}`, { value });
  }

  getUsers(role?: string, page = 0, size = 20): Observable<PageResponse<any>> {
    const params: Record<string, string | number> = { page, size };
    if (role) params['role'] = role;
    return this.api.get<PageResponse<any>>('/admin/users', params);
  }

  updateUserStatus(userId: number, status: string): Observable<any> {
    return this.api.put<any>(`/admin/users/${userId}/status`, { status });
  }

  getContracts(status?: string, page = 0, size = 20): Observable<PageResponse<any>> {
    const params: Record<string, string | number> = { page, size };
    if (status) params['status'] = status;
    return this.api.get<PageResponse<any>>('/admin/contracts', params);
  }

  getContract(id: number): Observable<any> {
    return this.api.get<any>(`/admin/contracts/${id}`);
  }
}
