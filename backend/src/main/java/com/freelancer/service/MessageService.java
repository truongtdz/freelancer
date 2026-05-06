package com.freelancer.service;

import com.freelancer.dto.request.SendMessageRequest;
import com.freelancer.dto.response.ConversationResponse;
import com.freelancer.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    ConversationResponse getOrCreateConversation(Long contractId, Long userId);
    Page<MessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable);
    MessageResponse sendMessage(Long conversationId, Long senderId, SendMessageRequest req);
}
