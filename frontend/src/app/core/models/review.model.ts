import { UserSummary } from './contract.model';

export type ReviewType = 'CLIENT_TO_FREELANCER' | 'FREELANCER_TO_CLIENT';

export interface Review {
  id: number;
  contractId: number;
  reviewer: UserSummary;
  reviewee?: UserSummary;
  rating: number;
  comment?: string;
  reviewType: ReviewType;
  createdAt: string;
}

export interface ReviewCreateRequest {
  rating: number;
  comment?: string;
}
