package com.project.user_service.exception;

public class EmailAlreadyExistException extends RuntimeException {
    public EmailAlreadyExistException() {
        super("User with this email already existed");
    }
}
