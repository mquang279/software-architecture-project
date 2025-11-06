package com.project.reservation_service.dto;

import com.project.reservation_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String payload;
    private Integer readFlag;
    private Instant createdAt;
}
