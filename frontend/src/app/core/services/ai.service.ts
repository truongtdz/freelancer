import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AiService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  suggestDescription(title: string, category: string): Observable<string> {
    return this.http
      .post<{ data: { suggestion: string } }>(`${this.base}/ai/suggest-description`, { title, category })
      .pipe(map(r => r.data.suggestion));
  }

  suggestCoverLetter(jobTitle: string, jobDescription: string, budgetMin: number, budgetMax: number): Observable<string> {
    return this.http
      .post<{ data: { suggestion: string } }>(`${this.base}/ai/suggest-cover-letter`, {
        jobTitle, jobDescription, budgetMin, budgetMax
      })
      .pipe(map(r => r.data.suggestion));
  }
}
