package com.ecomart.controller;

import com.ecomart.common.SecurityUtils;
import com.ecomart.dto.response.MessageResponse;
import com.ecomart.dto.response.NotificationResponse;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    public NotificationController(NotificationService notificationService, SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public PageResponse<NotificationResponse> myNotifications(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return notificationService.listForUser(securityUtils.currentUserId(), PageRequest.of(page, size));
    }

    @GetMapping("/unread-count")
    public long unreadCount() {
        return notificationService.unreadCount(securityUtils.currentUserId());
    }

    @PatchMapping("/{id}/read")
    public MessageResponse markRead(@PathVariable Long id) {
        notificationService.markRead(securityUtils.currentUserId(), id);
        return new MessageResponse("Đã đánh dấu đã đọc");
    }

    @PatchMapping("/read-all")
    public MessageResponse markAllRead() {
        notificationService.markAllRead(securityUtils.currentUserId());
        return new MessageResponse("Đã đánh dấu tất cả đã đọc");
    }
}
