package com.project.movie_reservation_system.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User ID is not valid");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long id) {
        super("User with id " + id + " not existed.");
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
