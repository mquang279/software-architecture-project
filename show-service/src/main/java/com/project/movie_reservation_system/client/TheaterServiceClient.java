package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.dto.TheaterDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "theater-service")
public interface TheaterServiceClient {
    @GetMapping("api/v1/theaters/{theaterId}")
    public TheaterDto getTheaterById(@PathVariable Long theaterId);
}
