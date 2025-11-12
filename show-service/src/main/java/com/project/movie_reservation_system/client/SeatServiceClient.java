package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.client.fallback.SeatServiceFallback;
import com.project.movie_reservation_system.dto.SeatDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "seat-service", fallback = SeatServiceFallback.class)
public interface SeatServiceClient {
    @GetMapping("/api/v1/seats/{id}")
    public SeatDto getSeatById(@PathVariable long id);

    @PostMapping("/api/v1/seats")
    public List<SeatDto> createSeatsWithGivenPrice(
            @RequestParam int seats,
            @RequestParam double price,
            @RequestParam String area,
            @RequestParam Long showId
    );
}
