package com.project.reservation_service.client.fallback;

import com.project.reservation_service.client.NotificationServiceClient;
import com.project.reservation_service.dto.NotificationDto;
import com.project.reservation_service.dto.NotificationRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceClientFallback  implements NotificationServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceClientFallback.class);

    @Override
    public NotificationDto addNotification(NotificationRequestDto request) {
        logger.error("NotificationService is DOWN! Cannot send notification for user ID: {}",
                request.getUserId());

        // ✅ Notification không critical, chỉ log warning thay vì throw exception
        logger.warn("Notification will be skipped. User won't receive email/SMS.");

        // Optional: Lưu vào queue để retry sau
        // notificationQueue.add(request);

        return null;
    }
}
