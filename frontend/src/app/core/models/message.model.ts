export interface ConversationResponse {
  id: number;
  contractId: number;
  clientId: number;
  freelancerId: number;
  lastMessageAt?: string;
  createdAt: string;
}

export interface MessageResponse {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  senderAvatar?: string;
  content: string;
  attachmentUrl?: string;
  isRead: boolean;
  createdAt: string;
}
