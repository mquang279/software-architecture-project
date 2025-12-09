package com.project.movie_reservation_system.entity;

import com.project.movie_reservation_system.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Enumerated(value = EnumType.STRING)
    private SeatStatus status;

    private Long reservationId;
    private Long showId;
    private double price;
    private int number;
    private String area;
}
