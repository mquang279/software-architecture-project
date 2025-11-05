package com.project.movie_reservation_system.exception;

public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException(long id) {
        super("Show with id " + id + " not exist");
    }
}
