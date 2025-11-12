package com.project.movie_reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatStructure {
    private int seatCount;
    private double seatPrice;
    private String area;
}
