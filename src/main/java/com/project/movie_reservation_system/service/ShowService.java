package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.ShowRequestDto;
import com.project.movie_reservation_system.entity.Seat;
import com.project.movie_reservation_system.entity.Show;
import com.project.movie_reservation_system.exception.MovieNotFoundException;
import com.project.movie_reservation_system.exception.ShowNotFoundException;
import com.project.movie_reservation_system.exception.TheaterNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.project.movie_reservation_system.constant.ExceptionMessages.*;

public interface ShowService {
    Show createNewShow(ShowRequestDto showRequestDto);

    PaginationResponse<Show> getllShows(int page, int size);

    void deleteShowById(long showId);

    PaginationResponse<Show> filterShowsByTheaterIdAndMovieId(Long theaterId, Long movieId, PageRequest pageRequest);

    Show getShowById(long showId);
}
