package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.NotificationRequestDto;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Notification;
import com.project.movie_reservation_system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> addNotification(@RequestBody NotificationRequestDto notificationRequestDTO) {
        return ResponseEntity.ok(notificationService.addNotification(notificationRequestDTO));
    }

    @GetMapping
    public PaginationResponse<Notification> getNotificationByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return notificationService.getNotificationsByUserId(userId, page, size);
    }

    @PutMapping
    public ResponseEntity<Notification> updateNotificationById(
            @RequestParam Long notificationId,
            @RequestBody NotificationRequestDto notificationRequestDTO
    ) {
        Notification response = notificationService.updateNotificationById(notificationId, notificationRequestDTO);
        return  ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotificationById(@PathVariable Long notificationId) {
        notificationService.deleteNotificationById(notificationId);
    }
}
