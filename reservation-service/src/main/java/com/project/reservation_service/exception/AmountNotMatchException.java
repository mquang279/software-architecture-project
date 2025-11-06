package com.project.reservation_service.exception;

import org.springframework.http.HttpStatus;

public class AmountNotMatchException extends RuntimeException{

    public AmountNotMatchException() {
        super("Amount not macth");
    }
}
