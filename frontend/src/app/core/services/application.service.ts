import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api.model';
import {
  Application,
  ApplicationCreateRequest,
  ContractResponse
} from '../models/application.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  /** FREELANCER: nộp ứng tuyển */
  apply(jobId: number, req: ApplicationCreateRequest): Observable<Application> {
    return this.http
      .post<ApiResponse<Application>>(`${this.base}/jobs/${jobId}/apply`, req)
      .pipe(map(r => r.data));
  }

  /** FREELANCER: rút ứng tuyển */
  withdrawApplication(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.base}/applications/${id}`)
      .pipe(map(() => void 0));
  }

  /** CLIENT: danh sách ứng tuyển cho job */
  getApplicationsByJob(
    jobId: number,
    page = 0,
    size = 10
  ): Observable<PageResponse<Application>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<Application>>>(
        `${this.base}/jobs/${jobId}/applications`,
        { params }
      )
      .pipe(map(r => r.data));
  }

  /** FREELANCER: danh sách ứng tuyển của mình */
  getMyApplications(
    status?: string,
    page = 0,
    size = 10
  ): Observable<PageResponse<Application>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http
      .get<ApiResponse<PageResponse<Application>>>(
        `${this.base}/my/applications`,
        { params }
      )
      .pipe(map(r => r.data));
  }

  /** CLIENT: chấp nhận ứng tuyển → trả ContractResponse */
  acceptApplication(id: number): Observable<ContractResponse> {
    return this.http
      .put<ApiResponse<ContractResponse>>(
        `${this.base}/applications/${id}/accept`,
        {}
      )
      .pipe(map(r => r.data));
  }

  /** CLIENT: từ chối ứng tuyển */
  rejectApplication(id: number, reason?: string): Observable<void> {
    return this.http
      .put<ApiResponse<void>>(
        `${this.base}/applications/${id}/reject`,
        { reason }
      )
      .pipe(map(() => void 0));
  }
}
