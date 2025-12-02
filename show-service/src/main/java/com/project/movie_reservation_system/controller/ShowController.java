package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.ShowRequestDto;
import com.project.movie_reservation_system.entity.Show;
import com.project.movie_reservation_system.service.ShowService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {
    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse<Show>> getAllShows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationResponse<Show> response = showService.getAllShows(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<PaginationResponse<Show>> filterShows(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationResponse<Show> response = showService.filterShowsByMovieIdAndStartTime(movieId, from, to,
                PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{showId}")
    public ResponseEntity<Show> getShowById(@PathVariable long showId) {
        Show show = showService.getShowById(showId);
        return ResponseEntity.ok(show);
    }

    @PostMapping("")
    public ResponseEntity<Show> createShow(@RequestBody ShowRequestDto showRequestDto) {
        Show show = showService.createNewShow(showRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(show);
    }

    @DeleteMapping("/{showId}")
    public ResponseEntity<?> deleteShowById(@PathVariable long showId) {
        showService.deleteShowById(showId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
