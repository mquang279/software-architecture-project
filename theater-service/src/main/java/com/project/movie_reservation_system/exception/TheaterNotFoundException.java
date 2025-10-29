package com.project.movie_reservation_system.exception;

public class TheaterNotFoundException extends RuntimeException {
    public TheaterNotFoundException(long id) {
        super("Cannot found theater with id " + id);
    }

    public TheaterNotFoundException() {
        super("Cannot found this theater");
    }
}
