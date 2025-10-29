package com.project.movie_reservation_system.dto;


import com.project.movie_reservation_system.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NotificationRequestDto {
    private Long userId;
    private NotificationType type;
    private String payload;
    private Integer readFlag = 0;
}
