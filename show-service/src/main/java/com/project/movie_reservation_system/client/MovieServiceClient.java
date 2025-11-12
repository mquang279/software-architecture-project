package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.client.fallback.MovieServiceFallback;
import com.project.movie_reservation_system.dto.MovieDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service", fallback = MovieServiceFallback.class)
public interface MovieServiceClient {
    @GetMapping("/api/v1/movies/{id}")
    public MovieDto getMovieById(@PathVariable Long id);
}
