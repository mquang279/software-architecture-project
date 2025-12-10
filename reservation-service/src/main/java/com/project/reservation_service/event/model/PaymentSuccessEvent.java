package com.project.reservation_service.event.model;

import com.project.reservation_service.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {
    private long reservationId;

    private long userId;

    private double amount;

    private final PaymentStatus status = PaymentStatus.SUCCESS;
}
