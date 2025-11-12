package com.project.reservation_service.client.fallback;

import com.project.reservation_service.client.ShowServiceClient;
import com.project.reservation_service.dto.ShowDto;
import com.project.reservation_service.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShowServiceClientFallback implements ShowServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ShowServiceClientFallback.class);

    @Override
    public ShowDto getShowById(Long id) {
        logger.error("🔴 ShowService is DOWN! Cannot get show ID: {}", id);
        throw new ServiceUnavailableException(id.toString());
    }
}
