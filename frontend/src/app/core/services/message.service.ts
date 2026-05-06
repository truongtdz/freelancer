import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { ConversationResponse, MessageResponse } from '../models/message.model';
import { PageResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class MessageService {
  private api = inject(ApiService);

  getOrCreateConversation(contractId: number): Observable<ConversationResponse> {
    return this.api.get<ConversationResponse>(`/contracts/${contractId}/conversation`);
  }

  getMessages(conversationId: number, page = 0, size = 50): Observable<PageResponse<MessageResponse>> {
    return this.api.get<PageResponse<MessageResponse>>(
      `/conversations/${conversationId}/messages`, { page, size }
    );
  }

  sendMessage(conversationId: number, content: string, attachmentUrl?: string): Observable<MessageResponse> {
    return this.api.post<MessageResponse>(`/conversations/${conversationId}/messages`, { content, attachmentUrl });
  }
}
