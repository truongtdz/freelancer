import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api.model';
import {
  JobDetail, JobListItem, JobCreateRequest, JobUpdateRequest, JobSearchParams
} from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class JobService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/jobs';

  searchJobs(params: JobSearchParams): Observable<PageResponse<JobListItem>> {
    return this.http
      .get<ApiResponse<PageResponse<JobListItem>>>(this.base, { params: this.buildParams(params) })
      .pipe(map(r => r.data));
  }

  getJob(id: number): Observable<JobDetail> {
    return this.http
      .get<ApiResponse<JobDetail>>(`${this.base}/${id}`)
      .pipe(map(r => r.data));
  }

  createJob(req: JobCreateRequest): Observable<JobDetail> {
    return this.http
      .post<ApiResponse<JobDetail>>(this.base, req)
      .pipe(map(r => r.data));
  }

  updateJob(id: number, req: JobUpdateRequest): Observable<JobDetail> {
    return this.http
      .put<ApiResponse<JobDetail>>(`${this.base}/${id}`, req)
      .pipe(map(r => r.data));
  }

  deleteJob(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.base}/${id}`)
      .pipe(map(() => void 0));
  }

  closeJob(id: number): Observable<JobDetail> {
    return this.http
      .post<ApiResponse<JobDetail>>(`${this.base}/${id}/close`, {})
      .pipe(map(r => r.data));
  }

  getMyJobs(params: JobSearchParams): Observable<PageResponse<JobListItem>> {
    return this.http
      .get<ApiResponse<PageResponse<JobListItem>>>(`${this.base}/my`, { params: this.buildParams(params) })
      .pipe(map(r => r.data));
  }

  private buildParams(p: JobSearchParams): HttpParams {
    let params = new HttpParams();
    if (p.keyword)    params = params.set('keyword', p.keyword);
    if (p.categoryId) params = params.set('categoryId', p.categoryId);
    if (p.skillIds?.length) params = params.set('skillIds', p.skillIds.join(','));
    if (p.budgetType) params = params.set('budgetType', p.budgetType);
    if (p.workMode)   params = params.set('workMode', p.workMode);
    if (p.budgetMin != null) params = params.set('budgetMin', p.budgetMin);
    if (p.budgetMax != null) params = params.set('budgetMax', p.budgetMax);
    if (p.status)     params = params.set('status', p.status);
    if (p.sort)       params = params.set('sort', p.sort);
    params = params.set('page', p.page ?? 0);
    params = params.set('size', p.size ?? 12);
    return params;
  }
}
