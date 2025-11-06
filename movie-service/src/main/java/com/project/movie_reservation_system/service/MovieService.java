package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.MovieRequestDto;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Movie;

public interface MovieService {
    PaginationResponse<Movie> getAllMovies(int page, int pageSize);

    Movie getMovieById(long id);

    Movie createMovie(MovieRequestDto movieRequestDto);

    Movie updateMovieById(long id, MovieRequestDto movieRequestDto);

    void deleteMovieById(long id);
}
