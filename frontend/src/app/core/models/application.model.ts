import { UserSummary } from './job.model';

export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN';

/** Body gửi khi apply */
export interface ApplicationCreateRequest {
  coverLetter: string;
  proposedPrice: number;
  estimatedDays: number;
  attachmentUrl?: string;
}

/** Full response from backend */
export interface Application {
  id: number;
  jobId: number;
  jobTitle: string;
  freelancer: UserSummary;
  freelancerRating?: number;
  freelancerCompletedJobs?: number;
  coverLetter: string;
  coverLetterPreview?: string;   // list-item response
  proposedPrice: number;
  estimatedDays: number;
  attachmentUrl?: string;
  status: ApplicationStatus;
  createdAt: string;
  updatedAt?: string;
}

/** ContractResponse placeholder — task 18 sẽ mở rộng */
export interface ContractResponse {
  id: number;
  contractCode: string;
  jobId: number;
  jobTitle: string;
  agreedPrice: number;
  commissionRate: number;
  commissionAmount: number;
  netAmount: number;
  startDate: string;
  endDate: string;
  status: string;
  createdAt: string;
}
