package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.client.MovieServiceClient;
import com.project.movie_reservation_system.client.SeatServiceClient;
import com.project.movie_reservation_system.client.TheaterServiceClient;
import com.project.movie_reservation_system.dto.*;
import com.project.movie_reservation_system.entity.Show;
import com.project.movie_reservation_system.exception.ShowNotFoundException;
import com.project.movie_reservation_system.repository.ShowRepository;
import com.project.movie_reservation_system.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.project.movie_reservation_system.constant.ExceptionMessages.*;

@Service
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final MovieServiceClient movieServiceClient;
    private final TheaterServiceClient theaterServiceClient;
    private final SeatServiceClient seatServiceClient;

    public ShowServiceImpl(ShowRepository showRepository, MovieServiceClient movieServiceClient,
            TheaterServiceClient theaterServiceClient, SeatServiceClient seatServiceClient) {
        this.showRepository = showRepository;
        this.movieServiceClient = movieServiceClient;
        this.theaterServiceClient = theaterServiceClient;
        this.seatServiceClient = seatServiceClient;
    }

    public Show createNewShow(ShowRequestDto showRequestDto) {
        MovieDto movie = movieServiceClient.getMovieById(showRequestDto.getMovieId());
        TheaterDto theater = theaterServiceClient.getTheaterById(showRequestDto.getTheaterId());
        List<SeatDto> seats = new ArrayList<>();
        showRequestDto.getSeats()
                .forEach(seatStructure -> seats.addAll(
                        seatServiceClient.createSeatsWithGivenPrice(
                                seatStructure.getSeatCount(),
                                seatStructure.getSeatPrice(),
                                seatStructure.getArea())));

        List<Long> seatIds = new ArrayList<>();
        for (SeatDto seat : seats) {
            seatIds.add(seat.getId());
        }

        Show show = Show.builder()
                .movieId(showRequestDto.getMovieId())
                .theaterId(showRequestDto.getTheaterId())
                .seatsIds(seatIds)
                .startTime(showRequestDto.getStartTime())
                .endTime(showRequestDto.getEndTime())
                .build();

        return showRepository.save(show);
    }

    public PaginationResponse<Show> getllShows(int page, int size) {
        Page<Show> showPage = showRepository.findAll(PageRequest.of(page, size));
        List<Show> shows = showPage.getContent();

        return PaginationResponse.<Show>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(showPage.getTotalPages())
                .totalElements(showPage.getTotalElements())
                .data(shows)
                .build();
    }

    public void deleteShowById(long showId) {
        showRepository.deleteById(showId);
    }

    public PaginationResponse<Show> filterShowsByTheaterIdAndMovieId(Long theaterId, Long movieId,
            PageRequest pageRequest) {
        Page<Show> showPage;

        if (theaterId == null && movieId == null) {
            showPage = showRepository.findAll(pageRequest);
        } else if (theaterId == null) {
            showPage = showRepository.findByMovieId(movieId, pageRequest);
        } else {
            showPage = showRepository.findByTheaterIdAndMovieId(theaterId, movieId, pageRequest);
        }

        return PaginationResponse.<Show>builder()
                .pageNumber(pageRequest.getPageNumber())
                .pageSize(pageRequest.getPageSize())
                .totalPages(showPage.getTotalPages())
                .totalElements(showPage.getTotalElements())
                .data(showPage.getContent())
                .build();
    }

    public Show getShowById(long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));
    }
}
