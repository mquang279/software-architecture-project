package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.dto.MovieRequestDto;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Movie;
import com.project.movie_reservation_system.enums.MovieGenre;
import com.project.movie_reservation_system.exception.MovieNotFoundException;
import com.project.movie_reservation_system.repository.MovieRepository;
import com.project.movie_reservation_system.service.MovieService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.project.movie_reservation_system.constant.ExceptionMessages.MOVIE_NOT_FOUND;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public PaginationResponse<Movie> getAllMovies(int page, int pageSize) {
        Page<Movie> moviePage = movieRepository.findAll(PageRequest.of(page, pageSize));
        List<Movie> movies = moviePage.getContent();
        return PaginationResponse.<Movie>builder()
                .pageNumber(page)
                .pageSize(pageSize)
                .totalPages(moviePage.getTotalPages())
                .totalElements(moviePage.getTotalElements())
                .data(movies)
                .build();
    }

    public Movie getMovieById(long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    public Movie createMovie(MovieRequestDto movieRequestDto) {

        Movie movie = Movie.builder()
                .movieLanguage(movieRequestDto.getMovieLanguage())
                .movieLength(movieRequestDto.getMovieLength())
                .genre(movieRequestDto.getGenre().stream().map(MovieGenre::valueOf).toList())
                .movieName(movieRequestDto.getMovieName())
                .releaseDate(movieRequestDto.getReleaseDate())
                .build();

        return movieRepository.save(movie);
    }

    public Movie updateMovieById(long movieId, MovieRequestDto movieRequestDto) {
        return movieRepository.findById(movieId)
                .map(movieInDb -> {
                    movieInDb.setMovieName(movieRequestDto.getMovieName());
                    movieInDb.setGenre(movieRequestDto.getGenre().stream().map(MovieGenre::valueOf).toList());
                    movieInDb.setMovieLanguage(movieRequestDto.getMovieLanguage());
                    movieInDb.setReleaseDate(movieRequestDto.getReleaseDate());
                    movieInDb.setMovieLength(movieRequestDto.getMovieLength());

                    return movieRepository.save(movieInDb);
                })
                .orElseThrow(() -> new MovieNotFoundException(movieId));
    }

    public void deleteMovieById(long movieId) {
        movieRepository.deleteById(movieId);
    }
}
