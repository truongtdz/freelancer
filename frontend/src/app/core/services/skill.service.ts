import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { Skill } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class SkillService {
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiUrl + '/skills';

  private cache$?: Observable<Skill[]>;

  getSkills(): Observable<Skill[]> {
    if (!this.cache$) {
      this.cache$ = this.http
        .get<ApiResponse<Skill[]>>(this.url)
        .pipe(map(r => r.data), shareReplay(1));
    }
    return this.cache$;
  }

  searchSkills(q: string): Observable<Skill[]> {
    return this.http
      .get<ApiResponse<Skill[]>>(`${this.url}/search`, { params: { q } })
      .pipe(map(r => r.data));
  }
}
