package com.project.movie_reservation_system.controller;

import com.project.movie_reservation_system.dto.ApiResponse;
import com.project.movie_reservation_system.dto.MovieRequestDto;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Movie;
import com.project.movie_reservation_system.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse<Movie>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PaginationResponse<Movie> response = movieService.getAllMovies(page, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable long id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @PostMapping("")
    public ResponseEntity<Movie> createNewMovie(@RequestBody MovieRequestDto movieRequestDto) {
        Movie movie = movieService.createMovie(movieRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movie);
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<Movie> updateMovieById(@PathVariable long id,
            @RequestBody MovieRequestDto movieRequestDto) {
        Movie movie = movieService.updateMovieById(id, movieRequestDto);
        return ResponseEntity.ok()
                .body(movie);
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<?> deleteMovieById(@PathVariable long movieId) {
        movieService.deleteMovieById(movieId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
