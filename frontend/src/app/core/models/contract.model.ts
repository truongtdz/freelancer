export type ContractStatus =
  | 'PENDING_PAYMENT'
  | 'IN_PROGRESS'
  | 'FREELANCER_SUBMITTED'
  | 'CLIENT_CONFIRMED'
  | 'PAID_OUT'
  | 'DISPUTED'
  | 'CANCELLED';

export type SubmissionStatus = 'PENDING_CONFIRM' | 'CONFIRMED' | 'REJECTED' | 'SUPERSEDED';
export type DisputeStatus    = 'OPEN' | 'RESOLVED' | 'CLOSED';
export type PayoutStatus     = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface UserSummary {
  id: number;
  username: string;
  fullName: string;
  avatarUrl?: string;
}

/** Used in GET /api/contracts (list) */
export interface ContractListItem {
  id: number;
  contractCode: string;
  jobId: number;
  jobTitle: string;
  counterparty: UserSummary;
  agreedPrice: number;
  status: ContractStatus;
  startDate: string;
  endDate: string;
  updatedAt: string;
}

export interface ProgressReport {
  id: number;
  contractId: number;
  title: string;
  content: string;
  progressPercentage: number;
  attachmentUrls: string[];
  clientFeedback?: string;
  createdAt: string;
}

export interface CompletionSubmission {
  id: number;
  contractId: number;
  summary: string;
  deliverableUrls: string[];
  qrCodeUrl?: string;
  status: SubmissionStatus;
  rejectionReason?: string;
  attemptNumber: number;
  createdAt: string;
}

export interface TransactionSummary {
  id: number;
  transactionCode: string;
  type: string;
  amount: number;
  status: string;
  createdAt: string;
}

export interface DisputeSummary {
  id: number;
  reason: string;
  description: string;
  status: DisputeStatus;
  resolution?: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface PayoutSummary {
  id: number;
  payoutCode: string;
  grossAmount: number;
  commissionAmount: number;
  netAmount: number;
  qrCodeUrl?: string;
  proofImageUrl?: string;
  status: PayoutStatus;
  paidAt?: string;
}

/** Used in GET /api/contracts/{id} (detail) */
export interface ContractDetail {
  id: number;
  contractCode: string;
  jobId: number;
  jobTitle: string;
  client: UserSummary;
  freelancer: UserSummary;
  agreedPrice: number;
  commissionRate?: number;
  commissionAmount?: number;
  netAmount?: number;
  startDate: string;
  endDate: string;
  status: ContractStatus;
  createdAt: string;
  updatedAt: string;

  progressReports: ProgressReport[];
  completionSubmissions: CompletionSubmission[];
  transactions: TransactionSummary[];
  payout?: PayoutSummary;
  dispute?: DisputeSummary;

  canPay: boolean;
  canReportProgress: boolean;
  canSubmitCompletion: boolean;
  canConfirmCompletion: boolean;
  canRejectCompletion: boolean;
  canRaiseDispute: boolean;
  submissionAttempts: number;
  maxSubmissionAttempts: number;
}
