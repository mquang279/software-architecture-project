package com.project.movie_reservation_system.event;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatsLockedEvent {
    private long reservationId;

    private List<Long> seatIds;
}
