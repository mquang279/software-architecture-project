package com.project.movie_reservation_system.exception;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException() {
        super("Cannot found this seat");
    }

    public SeatNotFoundException(String message) {
        super(message);
    }

    public SeatNotFoundException(Long id) {
        super("Seat with id " + id + " not existed");
    }

    public SeatNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
