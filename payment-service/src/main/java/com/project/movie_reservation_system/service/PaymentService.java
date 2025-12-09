package com.project.movie_reservation_system.service;

import java.util.List;

public interface PaymentService {
    void processPaymentForReservation(Long reservationId, Long userId, double totalPrice, List<Long> seatIds);
}