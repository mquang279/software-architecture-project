package com.project.reservation_service.exception;

public class SeatAlreadyBookedException extends RuntimeException {

    public SeatAlreadyBookedException() {
        super("One or more seats are already booked");
    }

    public SeatAlreadyBookedException(String message) {
        super(message);
    }
}
