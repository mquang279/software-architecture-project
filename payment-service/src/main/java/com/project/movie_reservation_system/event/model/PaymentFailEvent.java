package com.project.movie_reservation_system.event.model;

import com.project.movie_reservation_system.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailEvent {
    private long reservationId;

    private long userId;

    private double amount;

    private final PaymentStatus status = PaymentStatus.FAILED;
}
