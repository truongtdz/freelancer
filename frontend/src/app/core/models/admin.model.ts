import { UserSummary } from './contract.model';

export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalJobs: number;
  openJobs: number;
  totalContracts: number;
  contractsInProgress: number;
  pendingPayouts: number;
  openDisputes: number;
  totalRevenue: number;
  totalEscrow: number;
  dailyRevenue?: { date: string; amount: number }[];
}

export interface PendingPayoutItem {
  contractId: number;
  contractCode: string;
  freelancer: UserSummary;
  client: UserSummary;
  agreedPrice: number;
  commissionAmount: number;
  netAmount: number;
  qrCodeUrl?: string;
  bankInfoSnapshot?: string;
  confirmedAt: string;
}

export interface PayoutCreateRequest {
  proofImageUrl: string;
  note?: string;
}

export interface AdminPayout {
  id: number;
  payoutCode: string;
  contractId: number;
  contractCode: string;
  freelancer: UserSummary;
  grossAmount: number;
  commissionAmount: number;
  netAmount: number;
  proofImageUrl?: string;
  status: string;
  paidAt: string;
}

export type DisputeStatus = 'OPEN' | 'IN_REVIEW' | 'RESOLVED' | 'CLOSED';
export type DisputeResolutionType = 'FULL_REFUND' | 'FULL_PAYOUT' | 'PARTIAL';

export interface AdminDispute {
  id: number;
  contractId: number;
  reason: string;
  description: string;
  status: DisputeStatus;
  resolution?: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface DisputeResolveRequest {
  resolutionType: DisputeResolutionType;
  partialAmountToFreelancer?: number;
  resolution: string;
}

export interface SystemSetting {
  key: string;
  value: string;
  description?: string;
}
