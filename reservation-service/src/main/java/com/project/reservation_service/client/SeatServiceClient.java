package com.project.reservation_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.project.reservation_service.dto.SeatDto;

import java.util.List;

@FeignClient(name = "seat-service")
public interface SeatServiceClient {

    @GetMapping("/api/v1/seats/{seatId}")
    SeatDto getSeatById(@PathVariable Long seatId);

    @PostMapping("/api/v1/seats/lock")
    void lockSeats(@RequestBody List<Long> seatIds);

    @PostMapping("/api/v1/seats/unlock")
    void unlockSeats(@RequestBody List<Long> seatIds);

    @PutMapping("/api/v1/seats/status")
    void updateSeatStatus(@RequestBody List<Long> seatIds, @RequestParam String status);
}