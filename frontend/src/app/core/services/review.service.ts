import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PageResponse } from '../models/api.model';
import { Review, ReviewCreateRequest } from '../models/review.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private api = inject(ApiService);

  createReview(contractId: number, req: ReviewCreateRequest): Observable<Review> {
    return this.api.post<Review>(`/contracts/${contractId}/review`, req);
  }

  getReviewsByUser(userId: number, page = 0, size = 10): Observable<PageResponse<Review>> {
    return this.api.get<PageResponse<Review>>(`/users/${userId}/reviews`, { page, size });
  }
}
