package com.freelancer.service.impl;

import com.freelancer.dto.request.SendMessageRequest;
import com.freelancer.dto.response.ConversationResponse;
import com.freelancer.dto.response.MessageResponse;
import com.freelancer.entity.Conversation;
import com.freelancer.entity.Contract;
import com.freelancer.entity.Message;
import com.freelancer.entity.User;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import com.freelancer.repository.ConversationRepository;
import com.freelancer.repository.ContractRepository;
import com.freelancer.repository.MessageRepository;
import com.freelancer.repository.UserRepository;
import com.freelancer.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository      messageRepository;
    private final ContractRepository     contractRepository;
    private final UserRepository         userRepository;

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(Long contractId, Long userId) {
        Contract contract = contractRepository.findByIdAndDeletedAtIsNull(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        boolean isParticipant = Objects.equals(contract.getClientId(), userId)
                || Objects.equals(contract.getFreelancerId(), userId);
        if (!isParticipant) throw new AppException(ErrorCode.FORBIDDEN, "Bạn không phải thành viên hợp đồng này");

        Conversation conv = conversationRepository.findByContractId(contractId)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .contractId(contractId)
                        .jobId(contract.getJobId())
                        .clientId(contract.getClientId())
                        .freelancerId(contract.getFreelancerId())
                        .createdAt(LocalDateTime.now())
                        .build()));

        return toConversationResponse(conv);
    }

    @Override
    @Transactional
    public Page<MessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Conversation không tồn tại"));

        boolean isParticipant = Objects.equals(conv.getClientId(), userId)
                || Objects.equals(conv.getFreelancerId(), userId);
        if (!isParticipant) throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem cuộc trò chuyện này");

        messageRepository.markReadByReceiver(conversationId, userId);

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(m -> toMessageResponse(m));
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long senderId, SendMessageRequest req) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Conversation không tồn tại"));

        boolean isParticipant = Objects.equals(conv.getClientId(), senderId)
                || Objects.equals(conv.getFreelancerId(), senderId);
        if (!isParticipant) throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền gửi tin nhắn");

        Message msg = messageRepository.save(Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .content(req.getContent())
                .attachmentUrl(req.getAttachmentUrl())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        conv.setLastMessageAt(msg.getCreatedAt());
        conversationRepository.save(conv);

        return toMessageResponse(msg);
    }

    private ConversationResponse toConversationResponse(Conversation c) {
        return ConversationResponse.builder()
                .id(c.getId())
                .contractId(c.getContractId())
                .clientId(c.getClientId())
                .freelancerId(c.getFreelancerId())
                .lastMessageAt(c.getLastMessageAt())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private MessageResponse toMessageResponse(Message m) {
        User sender = userRepository.findById(m.getSenderId()).orElse(null);
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .senderId(m.getSenderId())
                .senderName(sender != null ? sender.getFullName() : "Unknown")
                .senderAvatar(sender != null ? sender.getAvatarUrl() : null)
                .content(m.getContent())
                .attachmentUrl(m.getAttachmentUrl())
                .isRead(m.isRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
