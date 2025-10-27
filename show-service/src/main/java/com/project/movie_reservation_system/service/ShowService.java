package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.ShowRequestDto;
import com.project.movie_reservation_system.entity.Show;
import org.springframework.data.domain.PageRequest;

public interface ShowService {
    Show createNewShow(ShowRequestDto showRequestDto);

    PaginationResponse<Show> getllShows(int page, int size);

    void deleteShowById(long showId);

    PaginationResponse<Show> filterShowsByTheaterIdAndMovieId(Long theaterId, Long movieId, PageRequest pageRequest);

    Show getShowById(long showId);
}
