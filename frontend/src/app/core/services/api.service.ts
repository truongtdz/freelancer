import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  get<T>(path: string, params?: Record<string, string | number | boolean>): Observable<T> {
    const httpParams = this.buildParams(params);
    return this.http
      .get<ApiResponse<T>>(`${this.base}${path}`, { params: httpParams })
      .pipe(map(r => r.data), catchError(this.handleError));
  }

  post<T>(path: string, body?: unknown, params?: Record<string, string | number | boolean>): Observable<T> {
    const httpParams = this.buildParams(params);
    return this.http
      .post<ApiResponse<T>>(`${this.base}${path}`, body, { params: httpParams })
      .pipe(map(r => r.data), catchError(this.handleError));
  }

  put<T>(path: string, body?: unknown): Observable<T> {
    return this.http
      .put<ApiResponse<T>>(`${this.base}${path}`, body)
      .pipe(map(r => r.data), catchError(this.handleError));
  }

  delete<T>(path: string): Observable<T> {
    return this.http
      .delete<ApiResponse<T>>(`${this.base}${path}`)
      .pipe(map(r => r.data), catchError(this.handleError));
  }

  upload<T>(path: string, file: File, params?: Record<string, string>): Observable<T> {
    const formData = new FormData();
    formData.append('file', file);
    const httpParams = params ? this.buildParams(params) : undefined;
    return this.http
      .post<ApiResponse<T>>(`${this.base}${path}`, formData, { params: httpParams })
      .pipe(map(r => r.data), catchError(this.handleError));
  }

  private buildParams(params?: Record<string, string | number | boolean>): HttpParams {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        if (v !== null && v !== undefined) {
          httpParams = httpParams.set(k, String(v));
        }
      });
    }
    return httpParams;
  }

  private handleError(err: { status: number; error: { message: string; data?: Record<string, string> } }) {
    const body = err?.error;
    let message = body?.message ?? 'Lỗi không xác định';
    if (body?.data && typeof body.data === 'object') {
      const fieldMsgs = Object.values(body.data).filter(Boolean);
      if (fieldMsgs.length > 0) message = fieldMsgs.join(', ');
    }
    return throwError(() => ({ status: err?.status, message }));
  }
}
