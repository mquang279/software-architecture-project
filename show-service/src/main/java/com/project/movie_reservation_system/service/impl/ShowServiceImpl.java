package com.project.movie_reservation_system.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.movie_reservation_system.client.MovieServiceClient;
import com.project.movie_reservation_system.client.SeatServiceClient;
import com.project.movie_reservation_system.client.TheaterServiceClient;
import com.project.movie_reservation_system.dto.*;
import com.project.movie_reservation_system.entity.Show;
import com.project.movie_reservation_system.exception.ShowNotFoundException;
import com.project.movie_reservation_system.repository.ShowRepository;
import com.project.movie_reservation_system.service.ShowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final MovieServiceClient movieServiceClient;
    private final TheaterServiceClient theaterServiceClient;
    private final SeatServiceClient seatServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ShowServiceImpl(ShowRepository showRepository, MovieServiceClient movieServiceClient,
            TheaterServiceClient theaterServiceClient, SeatServiceClient seatServiceClient,
            RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.showRepository = showRepository;
        this.movieServiceClient = movieServiceClient;
        this.theaterServiceClient = theaterServiceClient;
        this.seatServiceClient = seatServiceClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Show createNewShow(ShowRequestDto showRequestDto) {
        MovieDto movie = movieServiceClient.getMovieById(showRequestDto.getMovieId());
        TheaterDto theater = theaterServiceClient.getTheaterById(showRequestDto.getTheaterId());

        Show show = showRepository.save(Show.builder()
                .movieId(showRequestDto.getMovieId())
                .theaterId(showRequestDto.getTheaterId())
                .startTime(showRequestDto.getStartTime())
                .endTime(showRequestDto.getEndTime())
                .build());

        List<SeatDto> seats = new ArrayList<>();
        showRequestDto.getSeats()
                .forEach(seatStructure -> seats.addAll(
                        seatServiceClient.createSeatsWithGivenPrice(
                                seatStructure.getSeatCount(),
                                seatStructure.getSeatPrice(),
                                seatStructure.getArea(),
                                show.getId())));
        redisTemplate.opsForValue().set("show:id:" + show.getId(), show, 60,
                TimeUnit.SECONDS);
        redisTemplate.delete("shows:page:*");
        Set<String> keys = redisTemplate
                .keys("show:filter:movieId:" + movie.getId() + ":theaterId:" +
                        theater.getId() + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return show;
    }

    public PaginationResponse<Show> getAllShows(int page, int size) {
        String key = String.format("shows:page:%d:size:%d", page, size);
        Object cachedObject = redisTemplate.opsForValue().get(key);

        PaginationResponse<Show> response = objectMapper.convertValue(cachedObject,
                new TypeReference<PaginationResponse<Show>>() {
                });

        if (response != null) {
            return response;
        }

        Page<Show> showPage = showRepository.findAll(PageRequest.of(page, size));
        List<Show> shows = showPage.getContent();

        response = PaginationResponse.<Show>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(showPage.getTotalPages())
                .totalElements(showPage.getTotalElements())
                .data(shows)
                .build();
        redisTemplate.opsForValue().set(key, response, 60, TimeUnit.SECONDS);
        return response;
    }

    public void deleteShowById(long showId) {
        showRepository.deleteById(showId);
        redisTemplate.delete("show:id:" + showId);
    }

    public PaginationResponse<Show> filterShows(Long movieId, Instant from, Instant to, Long theaterId,
            PageRequest pageRequest) {
        String key = "show:filter:movieId:" + movieId + ":theaterId:" + theaterId +
                ":from:" + from + ":to:" + to
                + ":page:" + pageRequest.getPageNumber() + ":size:" +
                pageRequest.getPageSize();
        Object cachedObject = redisTemplate.opsForValue().get(key);

        PaginationResponse<Show> response = objectMapper.convertValue(cachedObject,
                new TypeReference<PaginationResponse<Show>>() {
                });

        if (response != null) {
            return response;
        }
        Page<Show> showPage;

        if (movieId != null && theaterId != null && from != null && to != null) {
            showPage = showRepository.findByMovieIdAndStartTimeBetweenAndTheaterId(movieId, from, to, theaterId,
                    pageRequest);
        } else if (movieId != null && from != null && to != null) {
            showPage = showRepository.findByMovieIdAndStartTimeBetween(movieId, from, to, pageRequest);
        } else if (movieId != null && theaterId != null) {
            showPage = showRepository.findByMovieIdAndTheaterId(movieId, theaterId, pageRequest);
        } else if (movieId != null) {
            showPage = showRepository.findByMovieId(movieId, pageRequest);
        } else if (theaterId != null) {
            showPage = showRepository.findByTheaterId(theaterId, pageRequest);
        } else if (from != null && to != null) {
            showPage = showRepository.findByStartTimeBetween(from, to, pageRequest);
        } else {
            showPage = showRepository.findAll(pageRequest);
        }

        response = PaginationResponse.<Show>builder()
                .pageNumber(pageRequest.getPageNumber())
                .pageSize(pageRequest.getPageSize())
                .totalPages(showPage.getTotalPages())
                .totalElements(showPage.getTotalElements())
                .data(showPage.getContent())
                .build();
        redisTemplate.opsForValue().set(key, response);
        return response;
    }

    public Show getShowById(long showId) {
        Object cachedObject = redisTemplate.opsForValue().get("show:id:" + showId);

        Show show = objectMapper.convertValue(cachedObject,
                new TypeReference<Show>() {
                });

        if (show != null) {
            return show;
        }

        show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));
        redisTemplate.opsForValue().set("show:id:" + showId, show, 60,
                TimeUnit.SECONDS);

        return show;
    }
}