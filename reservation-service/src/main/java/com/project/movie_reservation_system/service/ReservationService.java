package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.ReservationRequestDto;
import com.project.movie_reservation_system.entity.Reservation;

public interface ReservationService {
    Reservation createReservation(ReservationRequestDto reservation, Long userId);

    Reservation getReservationById(long id);

    Reservation cancelReservation(long id);

    PaginationResponse<Reservation> getAllReservationsForUser(Long userId, int page, int pageSize);
}
