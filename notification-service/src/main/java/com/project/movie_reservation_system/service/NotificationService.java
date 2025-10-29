package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.NotificationRequestDto;
import com.project.movie_reservation_system.dto.NotificationResponseDto;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Notification;

public interface NotificationService {
    Notification addNotification(NotificationRequestDto notificationRequestDTO);
    Notification getNotificationById(Long notificationId);
    PaginationResponse<Notification> getNotificationsByUserId(Long userId, int page, int size);
    Notification updateNotificationById(Long notificationId, NotificationRequestDto notificationRequestDTO);
    void deleteNotificationById(Long notificationId);
}
