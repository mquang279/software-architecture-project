package com.project.movie_reservation_system.event.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreatedEvent {
    private double amount;

    private long showId;

    private long userId;

    private List<Long> seatIds;

    private Long reservationId;
}
