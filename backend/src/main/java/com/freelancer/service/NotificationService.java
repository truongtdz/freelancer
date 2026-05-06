package com.freelancer.service;

import com.freelancer.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);
    long getUnreadCount(Long userId);
    void markRead(Long id, Long userId);
    void markAllRead(Long userId);
}
