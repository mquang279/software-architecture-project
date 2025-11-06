package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.PaymentRequestDto;
import com.project.movie_reservation_system.dto.PaymentResponseDto;
import com.project.movie_reservation_system.entity.Payment;


public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto requestDto, Long userId);
    PaymentResponseDto getPaymentById(Long paymentId);
    PaymentResponseDto getPaymentByReservationId(Long reservationId);
    PaginationResponse<Payment> getPaymentsByUserId(Long userId, int page, int size);
}