package com.project.movie_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequestDto {

    private long movieId;
    private long theaterId;
    private Instant startTime;
    private Instant endTime;
    private List<SeatStructure> seats;

}
