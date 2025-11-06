package com.project.reservation_service.exception;

import org.springframework.http.HttpStatus;

public class ShowStartedException extends RuntimeException{

    public ShowStartedException(Long id) {
        super("Show with id: " + id + "has started");
    }
}
