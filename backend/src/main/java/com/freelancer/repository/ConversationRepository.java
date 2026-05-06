package com.freelancer.repository;

import com.freelancer.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByContractId(Long contractId);
}
