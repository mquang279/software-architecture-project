package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Seat;
import com.project.movie_reservation_system.service.impl.SeatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/seats")
public class SeatController {

    private final SeatServiceImpl seatService;

    @Autowired
    public SeatController(SeatServiceImpl seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/{seatId}")
    public ResponseEntity<Seat> getSeatById(@PathVariable Long seatId) {
        Seat seat = seatService.getSeatById(seatId);
        return ResponseEntity.ok(seat);
    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse<Seat>> getSeatsByShowId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Long showId
    ) {
        return ResponseEntity.ok(seatService.getSeatsByShowId(showId, size, page));
    }

    @PostMapping("/lock")
    public void lockSeats(@RequestBody List<Long> seatIds) {
        seatService.lockSeats(seatIds);
    }

    @PostMapping("/unlock")
    public void unlockSeats(@RequestBody List<Long> seatIds) {
        seatService.unlockSeats(seatIds);
    }

    @PutMapping("/status")
    public ResponseEntity<Void> updateSeatStatus(
            @RequestBody List<Long> seatIds,
            @RequestParam String status) {
        seatService.updateSeatStatus(seatIds, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<List<Seat>> createSeatsWithGivenPrice(
            @RequestParam int seats,
            @RequestParam double price,
            @RequestParam String area,
            @RequestParam Long showId) {

        List<Seat> createdSeats = seatService.createSeatsWithGivenPrice(showId, seats, price, area);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSeats);
    }
}