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
    @ExceptionHandler(SeatAlreadyBookedException.class)
    public ResponseEntity<ExceptionResponse> handleSeatAlreadyBookedException(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Seat already booked")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleSeatNotFoundException(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Seat not found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(SeatLockAcquiredException.class)
    public ResponseEntity<ExceptionResponse> handleSeatLockAcquiredException(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Failed to lock seat")
                .status(HttpStatus.CONFLICT.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now()).build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
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
