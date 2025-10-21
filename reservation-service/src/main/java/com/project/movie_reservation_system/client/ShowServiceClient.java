package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.dto.ShowDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "show-service")
public interface ShowServiceClient {

    @GetMapping("/api/shows/{showId}")
    ShowDto getShowById(@PathVariable Long showId);
}