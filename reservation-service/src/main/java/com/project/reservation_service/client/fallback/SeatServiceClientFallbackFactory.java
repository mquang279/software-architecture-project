package com.project.reservation_service.client.fallback;

import com.project.reservation_service.client.SeatServiceClient;
import com.project.reservation_service.dto.SeatDto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SeatServiceClientFallbackFactory implements FallbackFactory<SeatServiceClient> {

    @Override
    public SeatServiceClient create(Throwable cause) {
        return new SeatServiceClient() {
            @Override
            public SeatDto getSeatById(Long seatId) {
                log.error("🔴 SeatService FAILED for seat ID: {}", seatId);
                log.error("🔴 Root cause: {}", cause.getClass().getName());
                log.error("🔴 Message: {}", cause.getMessage());
                
                if (cause instanceof FeignException.NotFound) {
                    log.error("❌ Seat not found: {}", seatId);
                } else if (cause instanceof FeignException.ServiceUnavailable) {
                    log.error("❌ Seat service is down!");
                } else if (cause instanceof java.net.ConnectException) {
                    log.error("❌ Cannot connect to seat-service!");
                }
                
                throw new RuntimeException("Seat service failed: " + cause.getMessage(), cause);
            }

            @Override
            public void lockSeats(List<Long> seatIds) {
                log.error("🔴 SeatService FAILED for lockSeats: {}", seatIds);
                log.error("🔴 Cause: {}", cause.getMessage());
                throw new RuntimeException("Cannot lock seats: " + cause.getMessage(), cause);
            }

            @Override
            public void unlockSeats(List<Long> seatIds) {
                log.error("🔴 SeatService FAILED for unlockSeats: {}", seatIds);
                log.error("🔴 Cause: {}", cause.getMessage());
                throw new RuntimeException("Cannot unlock seats: " + cause.getMessage(), cause);
            }

            @Override
            public void updateSeatStatus(List<Long> seatIds, String status) {
                log.error("🔴 SeatService FAILED for updateSeatStatus: {} - {}", seatIds, status);
                log.error("🔴 Cause: {}", cause.getMessage());
                throw new RuntimeException("Cannot update seat status: " + cause.getMessage(), cause);
            }
        };
    }
}