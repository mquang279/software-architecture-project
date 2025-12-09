package com.project.reservation_service.service;

import com.project.reservation_service.dto.PaginationResponse;
import com.project.reservation_service.dto.ReservationRequestDto;
import com.project.reservation_service.entity.Reservation;
import com.project.reservation_service.enums.PaymentStatus;

public interface ReservationService {
    Reservation createReservation(ReservationRequestDto reservation, Long userId);

    Reservation getReservationById(long id);

    PaginationResponse<Reservation> getAllReservationsForUser(Long userId, int page, int pageSize);

    void handlePaymentStatus(Long reservationId, PaymentStatus status);

    void handleSeatsStatus(Long reservationId, String status);
}
