package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.dto.TheaterRequestDto;
import com.project.movie_reservation_system.entity.Theater;
import com.project.movie_reservation_system.exception.TheaterNotFoundException;
import com.project.movie_reservation_system.repository.TheaterRepository;
import com.project.movie_reservation_system.service.TheaterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;

    public TheaterServiceImpl(TheaterRepository theaterRepository) {
        this.theaterRepository = theaterRepository;
    }

    public Theater createNewTheater(TheaterRequestDto theaterRequestDto) {
        Theater theater = Theater.builder()
                .name(theaterRequestDto.getName())
                .location(theaterRequestDto.getLocation())
                .build();
        return theaterRepository.save(theater);
    }

    public PaginationResponse<Theater> getAllTheaters(int page, int size) {
        Page<Theater> theaterPage = theaterRepository.findAll(PageRequest.of(page, size));
        List<Theater> theaters = theaterPage.getContent();

        return PaginationResponse.<Theater>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(theaterPage.getTotalPages())
                .totalElements(theaterPage.getTotalElements())
                .data(theaters)
                .build();
    }

    public PaginationResponse<Theater> getAllTheatersByLocation(int page, int size, String location) {
        Page<Theater> theaterPage = theaterRepository.findAllByLocation(location, PageRequest.of(page, size));
        List<Theater> theaters = theaterPage.getContent();

        return PaginationResponse.<Theater>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(theaterPage.getTotalPages())
                .totalElements(theaterPage.getTotalElements())
                .data(theaters)
                .build();
    }

    public Theater getTheaterById(long theaterId) {
        return theaterRepository.findById(theaterId)
                .orElseThrow(() -> new TheaterNotFoundException(theaterId));
    }

    public void deleteTheaterById(long theaterId) {
        theaterRepository.deleteById(theaterId);
    }

    public Theater updateTheaterById(long theaterId, TheaterRequestDto theaterRequestDto) {
        return theaterRepository.findById(theaterId)
                .map(theater -> {
                    theater.setName(theaterRequestDto.getName());
                    theater.setLocation(theater.getLocation());
                    return theaterRepository.save(theater);
                })
                .orElseThrow(() -> new TheaterNotFoundException(theaterId));
    }
}
