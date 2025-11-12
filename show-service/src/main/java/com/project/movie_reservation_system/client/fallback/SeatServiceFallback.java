package com.project.movie_reservation_system.client.fallback;

import com.project.movie_reservation_system.client.SeatServiceClient;
import com.project.movie_reservation_system.dto.SeatDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SeatServiceFallback implements SeatServiceClient {
    
    @Override
    public SeatDto getSeatById(long id) {
        log.error("Circuit breaker activated for seat-service. Unable to get seat with id: {}", id);
        
        // Return a default SeatDto
        SeatDto fallbackSeat = new SeatDto();
        fallbackSeat.setId(id);
        
        return fallbackSeat;
    }
    
    @Override
    public List<SeatDto> createSeatsWithGivenPrice(int seats, double price, String area, Long showId) {
        log.error("Circuit breaker activated for seat-service. Unable to create seats for show id: {}", showId);
        
        // Return empty list as fallback
        return Collections.emptyList();
    }
}
