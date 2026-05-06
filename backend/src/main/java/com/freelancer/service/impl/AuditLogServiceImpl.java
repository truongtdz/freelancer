package com.freelancer.service.impl;

import com.freelancer.entity.AuditLog;
import com.freelancer.repository.AuditLogRepository;
import com.freelancer.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entityType, Long entityId,
                    String oldValue, String newValue, String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Không để lỗi audit log làm fail business flow
            log.error("Failed to write audit log: action={}, entity={}:{}", action, entityType, entityId, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entityType, Long entityId, String ipAddress) {
        log(userId, action, entityType, entityId, null, null, ipAddress);
    }
}
