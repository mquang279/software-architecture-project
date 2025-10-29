package com.project.movie_reservation_system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MovieRequestDto {

    String movieName;
    List<String> genre;
    int movieLength;
    String movieLanguage;
    String releaseDate;

}
