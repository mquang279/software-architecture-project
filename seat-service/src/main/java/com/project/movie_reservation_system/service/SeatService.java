package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Seat;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface SeatService {
    Seat getSeatById(@PathVariable Long id);
    List<Seat> createSeatsWithGivenPrice(Long showId, int seats, double price, String area);
    PaginationResponse<Seat> getSeatsByShowId(Long showId, int size, int page);
    void lockSeats(List<Long> seatIds);
    void unlockSeats(List<Long> seatIds);
    void updateSeatStatus(List<Long> seatIds, String status);
}
