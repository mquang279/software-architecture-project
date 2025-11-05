package com.project.reservation_service.exception;

import com.project.reservation_service.dto.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandleException {
    @ExceptionHandler(ShowStartedException.class)
    public ResponseEntity<ExceptionResponse> handleShowStartedException(ShowStartedException e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Show has started")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(AmountNotMatchException.class)
    public ResponseEntity<ExceptionResponse> handleAmountNotMatchException(AmountNotMatchException e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Amount not match")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleReservationNotFoundException(ReservationNotFoundException e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Reservation not found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now())
                .build();

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
