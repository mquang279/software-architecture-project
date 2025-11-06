package com.project.user_service.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.project.user_service.dto.response.ExceptionResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserNotFoundException(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Cannot found user")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now()).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ExceptionResponse> handleEmailAlreadyExistException(Exception e, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .title("Email already exist")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(Instant.now()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
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
