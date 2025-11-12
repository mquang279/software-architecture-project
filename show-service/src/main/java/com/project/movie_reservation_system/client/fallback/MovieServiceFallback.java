package com.project.movie_reservation_system.client.fallback;

import com.project.movie_reservation_system.client.MovieServiceClient;
import com.project.movie_reservation_system.dto.MovieDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MovieServiceFallback implements MovieServiceClient {
    
    @Override
    public MovieDto getMovieById(Long id) {
        log.error("Circuit breaker activated for movie-service. Unable to get movie with id: {}", id);
        
        // Return a default MovieDto or throw a custom exception
        MovieDto fallbackMovie = new MovieDto();
        fallbackMovie.setId(id);
        fallbackMovie.setMovieName("Service Unavailable");
        
        return fallbackMovie;
    }
}
