export type NotificationType =
  | 'NEW_APPLICATION'
  | 'APPLICATION_ACCEPTED'
  | 'PAYMENT_RECEIVED'
  | 'PROGRESS_REPORT'
  | 'JOB_COMPLETED'
  | 'PAYOUT_COMPLETED'
  | 'NEW_MESSAGE'
  | 'SYSTEM';

export interface Notification {
  id: number;
  title: string;
  content: string;
  type: NotificationType;
  referenceType?: string;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}
