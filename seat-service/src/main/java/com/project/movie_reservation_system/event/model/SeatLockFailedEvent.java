package com.project.movie_reservation_system.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatLockFailedEvent {
    private long reservationId;

    private String message;
}
