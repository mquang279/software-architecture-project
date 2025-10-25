package com.project.reservation_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.reservation_service.dto.ShowDto;

@FeignClient(name = "show-service")
public interface ShowServiceClient {

    @GetMapping("/api/shows/{showId}")
    ShowDto getShowById(@PathVariable Long showId);
}