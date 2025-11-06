package com.project.movie_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.movie_reservation_system.enums.MovieGenre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id;
    String movieName;

    @Enumerated(value = EnumType.STRING)
    List<MovieGenre> genre;
    int movieLength;
    String movieLanguage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant releaseDate;
}
