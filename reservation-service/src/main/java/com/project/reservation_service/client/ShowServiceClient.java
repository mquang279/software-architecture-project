package com.project.reservation_service.client;

import com.project.reservation_service.client.fallback.ShowServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.reservation_service.dto.ShowDto;

@FeignClient(name = "show-service", fallback = ShowServiceClientFallback.class)
public interface ShowServiceClient {

    @GetMapping("/api/v1/shows/{showId}")
    ShowDto getShowById(@PathVariable Long showId);
}