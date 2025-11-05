package com.project.reservation_service.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends RuntimeException{
    public ReservationNotFoundException(Long id) {
        super("Reservation with id: " + id + "not found");
    }
}
