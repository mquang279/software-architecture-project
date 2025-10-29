package com.project.movie_reservation_system.exception;

public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException() {
        super("This seat has already booked");
    }
}
