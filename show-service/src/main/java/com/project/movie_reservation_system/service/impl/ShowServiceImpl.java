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
        redisTemplate.opsForValue().set("show:id:" + show.getId(), show, 60, TimeUnit.SECONDS);
        redisTemplate.delete("shows:page:*");
        redisTemplate.delete("show:theater:" + theater.getId() + ":movieId:" + movie.getId());
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

    public PaginationResponse<Show> filterShowsByMovieIdAndStartTime(Long movieId, Instant from, Instant to, PageRequest pageRequest) {
//        Object cachedObject = redisTemplate.opsForValue().get("show:theater:" + theaterId + ":movieId:" + movieId);
//
//        PaginationResponse<Show> response = objectMapper.convertValue(cachedObject,
//                new TypeReference<PaginationResponse<Show>>() {
//                });
//
//        if (response != null) {
//            return response;
//        }
        Page<Show> showPage;

        if (from == null && movieId == null) {
            showPage = showRepository.findAll(pageRequest);
        } else if (from == null) {
            showPage = showRepository.findByMovieId(movieId, pageRequest);
        } else {
            showPage = showRepository.findByMovieIdAndStartTimeBetween(movieId, from, to, pageRequest);
        }

        PaginationResponse<Show> response = PaginationResponse.<Show>builder()
                .pageNumber(pageRequest.getPageNumber())
                .pageSize(pageRequest.getPageSize())
                .totalPages(showPage.getTotalPages())
                .totalElements(showPage.getTotalElements())
                .data(showPage.getContent())
                .build();
//        redisTemplate.opsForValue().set("show:theater:" + theaterId + ":movieId:" + movieId, response);
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
        redisTemplate.opsForValue().set("show:id:" + showId, show, 60, TimeUnit.SECONDS);

        return show;
    }
}
