package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.ApiResponse;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.ShowRequestDto;
import com.project.movie_reservation_system.entity.Show;
import com.project.movie_reservation_system.service.impl.ShowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {

    private final ShowServiceImpl showService;

    @Autowired
    public ShowController(ShowServiceImpl showService) {
        this.showService = showService;
    }


    @GetMapping("/all")
    public ResponseEntity<PaginationResponse<Show>> getAllShows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PaginationResponse<Show> response = showService.getllShows(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<PaginationResponse<Show>> filterShows(
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String showDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PaginationResponse<Show> response = showService.filterShowsByTheaterIdAndMovieId(theaterId, movieId, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/show/{showId}")
    public ResponseEntity<ApiResponse<Show>> getShowById(@PathVariable long showId){
        Show show = showService.getShowById(showId);
        return ResponseEntity.ok(
                ApiResponse.success("Fetched show with id: " + showId, show)
        );
    }

    @PostMapping("/show/create")
    public ResponseEntity<ApiResponse<Show>> createShow(@RequestBody ShowRequestDto showRequestDto){
        Show show = showService.createNewShow(showRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success("Show created successfully: " + show.getId(), show)
                );
    }

    @PatchMapping("/show/update/movie/{showId}")
    public ResponseEntity<?> updateMovie(){
        return null;
    }

    @PatchMapping("/show/update/theatre/{showId}")
    public ResponseEntity<?> updateTheatre(){
        return null;
    }

    @PatchMapping("/show/update/timings/{showId}")
    public ResponseEntity<?> updateShowTimings(){
        return null;
    }

    @DeleteMapping("/show/delete/{showId}")
    public ResponseEntity<?> deleteShowById(@PathVariable long showId){
        showService.deleteShowById(showId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
