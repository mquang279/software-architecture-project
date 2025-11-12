package com.project.reservation_service.client;

import com.project.reservation_service.client.fallback.NotificationServiceClientFallback;
import com.project.reservation_service.dto.NotificationDto;
import com.project.reservation_service.dto.NotificationRequestDto;
import com.project.reservation_service.dto.ShowDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
;

@FeignClient(name = "notification-service", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {
    @PostMapping("/api/v1/notifications")
    NotificationDto addNotification(@RequestBody NotificationRequestDto notificationRequestDto);
}
