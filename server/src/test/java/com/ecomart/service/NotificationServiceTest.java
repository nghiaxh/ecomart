package com.ecomart.service;

import com.ecomart.domain.entity.Customer;
import com.ecomart.domain.entity.Notification;
import com.ecomart.exception.ResourceNotFoundException;
import com.ecomart.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(notificationRepository);
    }

    private Customer user(long id) {
        Customer u = new Customer();
        u.setId(id);
        return u;
    }

    private Notification notification(long id, long userId) {
        Notification n = new Notification();
        n.setId(id);
        n.setUser(user(userId));
        n.setRead(false);
        return n;
    }

    @Test
    void markReadOwnNotificationFlagsAsRead() {
        Notification notification = notification(3L, 5L);
        when(notificationRepository.findById(3L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markRead(5L, 3L);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markReadForeignNotificationThrowsNotFound() {
        when(notificationRepository.findById(3L)).thenReturn(Optional.of(notification(3L, 99L)));

        assertThrows(ResourceNotFoundException.class, () -> service.markRead(5L, 3L));
    }

    @Test
    void unreadCountDelegatesToRepository() {
        when(notificationRepository.countByUserIdAndIsReadFalse(5L)).thenReturn(3L);

        assertEquals(3L, service.unreadCount(5L));
    }
}