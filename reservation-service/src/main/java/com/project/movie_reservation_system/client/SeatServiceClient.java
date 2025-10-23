package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.dto.SeatDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "seat-service")
public interface SeatServiceClient {

    @GetMapping("/api/seats/{seatId}")
    SeatDto getSeatById(@PathVariable Long seatId);

    @PostMapping("/api/seats/lock")
    void lockSeats(@RequestBody List<Long> seatIds);

    @PostMapping("/api/seats/unlock")
    void unlockSeats(@RequestBody List<Long> seatIds);

    @PutMapping("/api/seats/status")
    void updateSeatStatus(@RequestBody List<Long> seatIds, @RequestParam String status);
}