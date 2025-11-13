package com.project.reservation_service.client.fallback;

import com.project.reservation_service.client.SeatServiceClient;
import com.project.reservation_service.dto.SeatDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SeatServiceClientFallback implements SeatServiceClient {

    @Override
    public SeatDto getSeatById(Long seatId) {
        log.error("🔴 SeatService Fallback triggered for getSeatById: {}", seatId);
        log.error("🔴 This means: Circuit is OPEN or service is not responding!");
        throw new RuntimeException("Seat service is currently unavailable. Seat ID: " + seatId);
    }

    @Override
    public void lockSeats(List<Long> seatIds) {
        log.error("🔴 SeatService Fallback triggered for lockSeats: {}", seatIds);
        throw new RuntimeException("Seat service is currently unavailable. Cannot lock seats.");
    }

    @Override
    public void unlockSeats(List<Long> seatIds) {
        log.error("🔴 SeatService Fallback triggered for unlockSeats: {}", seatIds);
        throw new RuntimeException("Seat service is currently unavailable. Cannot unlock seats.");
    }

    @Override
    public void updateSeatStatus(List<Long> seatIds, String status) {
        log.error("🔴 SeatService Fallback triggered for updateSeatStatus: {} - {}", seatIds, status);
        throw new RuntimeException("Seat service is currently unavailable. Cannot update seat status.");
    }
}