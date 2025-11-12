package com.project.reservation_service.client.fallback;


import com.project.reservation_service.client.UserServiceClient;
import com.project.reservation_service.dto.UserDto;
import com.project.reservation_service.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClientFallback implements UserServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public UserDto getUserById(Long id) {
        logger.error("🔴 UserService is DOWN! Cannot get user ID: {}", id);
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(id);
        fallbackUser.setEmail("unknown@system.com");
        fallbackUser.setFirstName("Unknown User (Service Unavailable)");

        return fallbackUser;
    }
}
