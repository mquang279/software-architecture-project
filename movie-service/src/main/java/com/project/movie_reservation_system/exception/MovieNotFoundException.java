package com.project.movie_reservation_system.exception;

public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(long id) {
        super("Movie with id " + id + " not found");
    }
}
