package com.project.reservation_service.exception;

import org.springframework.http.HttpStatus;

public class SeatLockAcquiredException extends CustomException{

    public SeatLockAcquiredException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
