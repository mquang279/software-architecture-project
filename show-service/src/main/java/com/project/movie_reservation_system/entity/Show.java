package com.project.movie_reservation_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    Long movieId;
    Long theaterId;
    Instant startTime;
    Instant endTime;
}
