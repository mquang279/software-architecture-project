package com.project.movie_reservation_system.client.fallback;

import com.project.movie_reservation_system.client.TheaterServiceClient;
import com.project.movie_reservation_system.dto.TheaterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TheaterServiceFallback implements TheaterServiceClient {
    
    @Override
    public TheaterDto getTheaterById(Long theaterId) {
        log.error("Circuit breaker activated for theater-service. Unable to get theater with id: {}", theaterId);
        
        // Return a default TheaterDto
        TheaterDto fallbackTheater = new TheaterDto();
        fallbackTheater.setId(theaterId);
        fallbackTheater.setName("Service Unavailable");
        fallbackTheater.setLocation("Theater service is temporarily unavailable");
        
        return fallbackTheater;
    }
}
