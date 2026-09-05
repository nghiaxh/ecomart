package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.domain.entity.Notification;
import com.ecomart.domain.entity.User;
import com.ecomart.domain.enums.NotificationType;
import com.ecomart.dto.response.NotificationResponse;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void send(User user, String title, String message, NotificationType type, String referenceId) {
        Notification notification = Mapper.newNotification(user, title, message, type, referenceId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listForUser(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return Mapper.toPage(page, page.getContent().stream().map(Mapper::toNotification).toList());
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .filter(notif -> notif.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }
}
