package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.entity.Movie;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service")
public interface MovieServiceClient {
    @GetMapping("/api/v1/movies/{id}")
    public MovieDto getMovie(@PathVariable Long id);

}
