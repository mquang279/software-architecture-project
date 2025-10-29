package com.project.user_service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Cannot found this user");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long id) {
        super("User with id " + id + " not existed");
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
