package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.dto.MovieRequestDto;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Movie;
import com.project.movie_reservation_system.enums.MovieGenre;
import com.project.movie_reservation_system.exception.MovieNotFoundException;
import com.project.movie_reservation_system.repository.MovieRepository;
import com.project.movie_reservation_system.service.MovieService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public MovieServiceImpl(MovieRepository movieRepository, RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.movieRepository = movieRepository;
        this.objectMapper = objectMapper;
    }

    public PaginationResponse<Movie> getAllMovies(int page, int pageSize) {
        String key = String.format("movies:page:%d:size:%d", page, pageSize);
        Object cachedObject = redisTemplate.opsForValue().get(key);

        PaginationResponse<Movie> response = objectMapper.convertValue(cachedObject,
                new TypeReference<PaginationResponse<Movie>>() {
                });

        if (response != null) {
            return response;
        }

        Page<Movie> moviePage = movieRepository.findAll(PageRequest.of(page, pageSize));
        List<Movie> movies = moviePage.getContent();
        response = PaginationResponse.<Movie>builder()
                .pageNumber(page)
                .pageSize(pageSize)
                .totalPages(moviePage.getTotalPages())
                .totalElements(moviePage.getTotalElements())
                .data(movies)
                .build();

        redisTemplate.opsForValue().set(key, response, 60, TimeUnit.SECONDS);

        return response;
    }

    public Movie getMovieById(long id) {
        String key = String.format("movie:id:%d", id);

        Object cachedObject = redisTemplate.opsForValue().get(key);
        Movie movie = null;

        if (cachedObject != null) {
            try {
                if (cachedObject instanceof Map) {
                    movie = objectMapper.convertValue(cachedObject, Movie.class);
                } else if (cachedObject instanceof Movie) {
                    movie = (Movie) cachedObject;
                }
            } catch (Exception e) {
                movie = null;
            }
        }

        if (movie == null) {
            movie = movieRepository.findById(id)
                    .orElseThrow(() -> new MovieNotFoundException(id));
            redisTemplate.opsForValue().set(key, movie, 60, TimeUnit.SECONDS);
        }
        return movie;
    }

    public Movie createMovie(MovieRequestDto movieRequestDto) {
        Movie movie = Movie.builder()
                .movieLanguage(movieRequestDto.getMovieLanguage())
                .movieLength(movieRequestDto.getMovieLength())
                .genre(movieRequestDto.getGenre().stream().map(MovieGenre::valueOf).toList())
                .movieName(movieRequestDto.getMovieName())
                .releaseDate(movieRequestDto.getReleaseDate())
                .build();

        Movie saved = movieRepository.save(movie);

        redisTemplate.opsForValue().set("movie:id:" + saved.getId(), saved, 60, TimeUnit.SECONDS);
        // Optimization: Avoid keys scan
        // redisTemplate.delete(redisTemplate.keys("movies:page:*"));
        return saved;
    }

    public Movie updateMovieById(long movieId, MovieRequestDto movieRequestDto) {
        return movieRepository.findById(movieId)
                .map(movieInDb -> {
                    movieInDb.setMovieName(movieRequestDto.getMovieName());
                    movieInDb.setGenre(movieRequestDto.getGenre().stream().map(MovieGenre::valueOf).toList());
                    movieInDb.setMovieLanguage(movieRequestDto.getMovieLanguage());
                    movieInDb.setReleaseDate(movieRequestDto.getReleaseDate());
                    movieInDb.setMovieLength(movieRequestDto.getMovieLength());

                    Movie updated = movieRepository.save(movieInDb);
                    redisTemplate.opsForValue().set("movie:id:" + updated.getId(), updated, 60, TimeUnit.SECONDS);
                    redisTemplate.delete(redisTemplate.keys("movies:page:*"));
                    return updated;
                })
                .orElseThrow(() -> new MovieNotFoundException(movieId));
    }

    public void deleteMovieById(long movieId) {
        movieRepository.deleteById(movieId);
        redisTemplate.delete("movie:id:" + movieId);
        redisTemplate.delete(redisTemplate.keys("movies:page:*"));
    }
}
