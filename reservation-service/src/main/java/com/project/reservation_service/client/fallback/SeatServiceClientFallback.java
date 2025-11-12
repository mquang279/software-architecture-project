package com.project.reservation_service.client.fallback;

import com.project.reservation_service.client.SeatServiceClient;
import com.project.reservation_service.dto.SeatDto;
import com.project.reservation_service.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class SeatServiceClientFallback implements SeatServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(SeatServiceClientFallback.class);

    @Override
    public SeatDto getSeatById(Long id) {
        logger.error("🔴 SeatService is DOWN! Cannot get seat ID: {}", id);
        throw new ServiceUnavailableException(id.toString());
    }

    @Override
    public void lockSeats(@RequestBody List<Long> seatIds) {
        logger.error("🔴 SeatService is DOWN! Cannot lock seats");
        throw new ServiceUnavailableException("Seat service is temporarily unavailable");
    }

    @Override
    public void unlockSeats(@RequestBody List<Long> seatIds) {
        logger.error("🔴 SeatService is DOWN! Cannot unlock seats");
        throw new ServiceUnavailableException("Seat service is temporarily unavailable");
    }

    @Override
    public void updateSeatStatus(@RequestBody List<Long> seatIds, @RequestParam String status) {
        logger.error("🔴 SeatService is DOWN! Cannot update seat status");
        throw new ServiceUnavailableException("Seat service is temporarily unavailable");
    }
}
