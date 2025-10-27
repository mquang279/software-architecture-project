package com.project.reservation_service.service;

import com.project.reservation_service.dto.PaginationResponse;
import com.project.reservation_service.dto.ReservationRequestDto;
import com.project.reservation_service.entity.Reservation;

public interface ReservationService {
    Reservation createReservation(ReservationRequestDto reservation, Long userId);

    Reservation getReservationById(long id);

    Reservation cancelReservation(long id);

    PaginationResponse<Reservation> getAllReservationsForUser(Long userId, int page, int pageSize);
}
