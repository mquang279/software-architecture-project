package com.project.movie_reservation_system.dto;

import com.project.movie_reservation_system.enums.MovieGenre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {
    Long id;
    String movieName;
    List<MovieGenre> genre;
    int movieLength;
    String movieLanguage;
    Instant releaseDate;
}
