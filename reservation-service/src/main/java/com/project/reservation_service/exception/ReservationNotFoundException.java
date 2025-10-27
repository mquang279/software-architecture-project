package com.project.reservation_service.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends CustomException{

    public ReservationNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
