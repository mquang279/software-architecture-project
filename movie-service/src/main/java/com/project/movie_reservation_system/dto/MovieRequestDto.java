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
public class MovieRequestDto {
    String movieName;
    List<String> genre;
    int movieLength;
    String movieLanguage;
    Instant releaseDate;
}
