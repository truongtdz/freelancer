import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { UserProfile, UserProfileUpdateRequest, PaymentInfo, PaymentInfoRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private api = inject(ApiService);

  getProfile(id: number | string): Observable<UserProfile> {
    return this.api.get<UserProfile>(`/users/${id}`);
  }

  updateMyProfile(req: UserProfileUpdateRequest): Observable<UserProfile> {
    return this.api.put<UserProfile>('/users/me/profile', req);
  }

  getMyPaymentInfos(): Observable<PaymentInfo[]> {
    return this.api.get<PaymentInfo[]>('/users/me/payment-info');
  }

  savePaymentInfo(req: PaymentInfoRequest): Observable<PaymentInfo> {
    return this.api.post<PaymentInfo>('/users/me/payment-info', req);
  }

  deletePaymentInfo(id: number): Observable<void> {
    return this.api.delete<void>(`/users/me/payment-info/${id}`);
  }
}
