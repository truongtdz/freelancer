import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { Category } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiUrl + '/categories';

  private cache$?: Observable<Category[]>;

  getCategories(): Observable<Category[]> {
    if (!this.cache$) {
      this.cache$ = this.http
        .get<ApiResponse<Category[]>>(this.url)
        .pipe(map(r => r.data), shareReplay(1));
    }
    return this.cache$;
  }
}
