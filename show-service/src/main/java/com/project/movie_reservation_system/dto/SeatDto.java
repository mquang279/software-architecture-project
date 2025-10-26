package com.project.movie_reservation_system.dto;

import com.project.movie_reservation_system.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto {
    private long id;
    private SeatStatus status;
    private double price;
    private int number;
    private String area;

}
