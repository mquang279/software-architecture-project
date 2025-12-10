package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.ShowRequestDto;
import com.project.movie_reservation_system.entity.Show;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;

public interface ShowService {
    Show createNewShow(ShowRequestDto showRequestDto);

    PaginationResponse<Show> getAllShows(int page, int size);

    void deleteShowById(long showId);

    PaginationResponse<Show> filterShows(Long movieId, Instant from, Instant to, Long theaterId, PageRequest pageRequest);

    Show getShowById(long showId);
}
