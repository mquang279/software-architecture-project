package com.project.movie_reservation_system.service;

import com.project.movie_reservation_system.entity.Seat;

import java.util.List;

public interface SeatService {
    List<Seat> createSeatsWithGivenPrice(int seats, double price, String area);

}
