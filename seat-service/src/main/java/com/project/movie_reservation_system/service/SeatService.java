package com.project.movie_reservation_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Seat;
import com.project.movie_reservation_system.enums.PaymentStatus;

import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface SeatService {
    Seat getSeatById(@PathVariable Long id);

    List<Seat> createSeatsWithGivenPrice(Long showId, int seats, double price, String area);

    PaginationResponse<Seat> getSeatsByShowId(Long showId, int size, int page);

    void processSeatLocking(Long reservationId, Long userId, List<Long> seatIds);

    void handlePaymentStatus(Long reservationId, List<Long> seatIds, PaymentStatus paymentStatus);
}
