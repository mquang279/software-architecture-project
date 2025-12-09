package com.project.reservation_service.event.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatsLockedEvent {
    private long reservationId;

    private long userId;

    private double totalPrice;

    private List<Long> seatIds;
}
