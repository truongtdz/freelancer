package com.freelancer.repository;

import com.freelancer.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.senderId <> :userId AND m.isRead = false")
    void markReadByReceiver(Long conversationId, Long userId);
}
