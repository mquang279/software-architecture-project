package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.client.UserServiceClient;
import com.project.movie_reservation_system.dto.NotificationRequestDto;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.UserDto;
import com.project.movie_reservation_system.entity.Notification;
import com.project.movie_reservation_system.exception.NotificationNotFoundException;
import com.project.movie_reservation_system.repository.NotificationRepository;
import com.project.movie_reservation_system.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserServiceClient userServiceClient;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserServiceClient userServiceClient) {
        this.notificationRepository = notificationRepository;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public Notification addNotification(NotificationRequestDto notificationRequestDTO) {
        UserDto user = userServiceClient.getUserById(notificationRequestDTO.getUserId());
        Notification notification = Notification.builder()
                .userId(user.getId())
                .type(notificationRequestDTO.getType())
                .payload(notificationRequestDTO.getPayload())
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification updateNotificationById(Long notificationId, NotificationRequestDto notificationRequestDTO) {

        Notification updatedNotification = notificationRepository.findById(notificationId)
                .map(notificationInDb -> {
                    if (notificationRequestDTO.getPayload() != null) {
                        notificationInDb.setPayload(notificationRequestDTO.getPayload());
                    }
                    if (notificationRequestDTO.getReadFlag() != null) {
                        notificationInDb.setReadFlag(notificationRequestDTO.getReadFlag());
                    }
                    return notificationRepository.save(notificationInDb);
                })
                .orElseThrow(() -> new NotificationNotFoundException());

        return updatedNotification;
    }

    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException());
    }

    public PaginationResponse<Notification> getNotificationsByUserId(Long userId, int page, int size) {
        Page<Notification> notificationPage = notificationRepository.findByUserId(userId, PageRequest.of(page, size));
        List<Notification> notifications = notificationPage.getContent();

        return new PaginationResponse<Notification>(
                page,
                size,
                notificationPage.getTotalPages(),
                notificationPage.getTotalElements(),
                notifications);

    }

    @Transactional
    public void deleteNotificationById(Long notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Delete notification failed");
        }
    }
}
