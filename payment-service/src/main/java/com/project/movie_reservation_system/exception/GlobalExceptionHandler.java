package com.project.movie_reservation_system.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.project.movie_reservation_system.dto.ExceptionResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ShowNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleShowNotFoundException(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Show not found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleUnwantedException(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
    }
}
