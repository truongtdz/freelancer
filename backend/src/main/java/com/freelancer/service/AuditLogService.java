package com.freelancer.service;

public interface AuditLogService {

    /**
     * Ghi audit log cho action tài chính / quan trọng.
     *
     * @param userId     user thực hiện hành động (null nếu system)
     * @param action     tên action (VD: "PAYMENT_SUCCESS", "CONTRACT_ACTIVATED")
     * @param entityType loại entity (VD: "CONTRACT", "TRANSACTION")
     * @param entityId   ID của entity
     * @param oldValue   JSON cũ (nullable)
     * @param newValue   JSON mới (nullable)
     * @param ipAddress  IP address (nullable)
     */
    void log(Long userId, String action, String entityType, Long entityId,
             String oldValue, String newValue, String ipAddress);

    /** Overload không cần old/new value (dùng cho action đơn giản). */
    void log(Long userId, String action, String entityType, Long entityId, String ipAddress);
}
