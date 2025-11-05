package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.ApiResponse;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.TheaterRequestDto;
import com.project.movie_reservation_system.entity.Theater;
import com.project.movie_reservation_system.service.impl.TheaterServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/theaters")
public class TheaterController {

    private final TheaterServiceImpl theaterService;

    @Autowired
    public TheaterController(TheaterServiceImpl theaterService) {
        this.theaterService = theaterService;
    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse<Theater>> getAllTheaters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginationResponse<Theater> response = theaterService.getAllTheaters(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/locations")
    public ResponseEntity<PaginationResponse<Theater>> getAllTheatersByLocation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String location
    ){
        PaginationResponse<Theater> response = theaterService.getAllTheatersByLocation(page, size, location);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{theaterId}")
    public ResponseEntity<Theater> getTheaterById(@PathVariable long theaterId){
        Theater theater = theaterService.getTheaterById(theaterId);
        return ResponseEntity.ok(theater);
    }


    @PostMapping("")
    public ResponseEntity<Theater> createTheater(@RequestBody TheaterRequestDto theaterRequestDto){
        Theater theater = theaterService.createNewTheater(theaterRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(theater);

    }

    @PutMapping("{theaterId}")
    public ResponseEntity<Theater> updateTheaterById(@PathVariable long theaterId, @RequestBody TheaterRequestDto theaterRequestDto){
        Theater updatedTheater = theaterService.updateTheaterById(theaterId, theaterRequestDto);
        return ResponseEntity.ok(updatedTheater);
    }


    @DeleteMapping("{theaterId}")
    public ResponseEntity<?> deleteTheaterById(@PathVariable long theaterId){
        theaterService.deleteTheaterById(theaterId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }


}
