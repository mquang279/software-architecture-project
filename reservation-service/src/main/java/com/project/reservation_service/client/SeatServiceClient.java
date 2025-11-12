package com.project.reservation_service.client;

import com.project.reservation_service.client.fallback.SeatServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.project.reservation_service.dto.SeatDto;

import java.util.List;

@FeignClient(
    name = "seat-service",
    path = "/api/v1/seats",
    fallbackFactory = SeatServiceClientFallbackFactory.class
)
public interface SeatServiceClient {

    @GetMapping("/{seatId}")
    SeatDto getSeatById(@PathVariable("seatId") Long seatId);

    @PostMapping("/lock")
    void lockSeats(@RequestBody List<Long> seatIds);

    @PostMapping("/unlock")
    void unlockSeats(@RequestBody List<Long> seatIds);

    @PutMapping("/status")
    void updateSeatStatus(@RequestBody List<Long> seatIds, @RequestParam("status") String status);
}