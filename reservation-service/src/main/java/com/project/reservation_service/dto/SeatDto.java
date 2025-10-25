package com.project.reservation_service.dto;

import com.project.reservation_service.enums.SeatStatus;

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
