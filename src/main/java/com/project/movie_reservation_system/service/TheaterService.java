package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.TheaterRequestDto;
import com.project.movie_reservation_system.entity.Theater;
import com.project.movie_reservation_system.exception.TheaterNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import static com.project.movie_reservation_system.constant.ExceptionMessages.THEATER_NOT_FOUND;

public interface TheaterService {
    Theater createNewTheater(TheaterRequestDto theaterRequestDto);

    PaginationResponse<Theater> getAllTheaters(int page, int size);

    PaginationResponse<Theater> getAllTheatersByLocation(int page, int size, String location);

    Theater getTheaterById(long theaterId);

    void deleteTheaterById(long theaterId);

    Theater updateTheaterById(long theaterId, TheaterRequestDto theaterRequestDto);
}
