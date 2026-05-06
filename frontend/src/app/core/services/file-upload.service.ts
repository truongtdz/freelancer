import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { UploadResponse } from '../models/job.model';

export type UploadSubFolder =
  | 'avatars' | 'qr-codes' | 'attachments'
  | 'deliverables' | 'payment-proof' | 'general';

@Injectable({ providedIn: 'root' })
export class FileUploadService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/files';

  uploadFile(file: File, subFolder: UploadSubFolder = 'general'): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<UploadResponse>>(`${this.base}/upload?subFolder=${subFolder}`, formData)
      .pipe(map(r => r.data));
  }

  deleteFile(subFolder: string, storedName: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.base}/${subFolder}/${storedName}`)
      .pipe(map(() => void 0));
  }
}
