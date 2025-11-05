package com.project.movie_reservation_system.dto;

import com.project.movie_reservation_system.enums.PaymentMethod;
import com.project.movie_reservation_system.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDto {
    private Long paymentId;
    private Long reservationId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant paidAt;
    private String message;
}