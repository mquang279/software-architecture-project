package com.project.movie_reservation_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "show", indexes = {
        @Index(name = "idx_show_movie_theater_time", columnList = "movie_id, theater_id, start_time"),
        @Index(name = "idx_show_theater", columnList = "theater_id")
})
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    Long movieId;
    Long theaterId;
    Instant startTime;
    Instant endTime;
}
