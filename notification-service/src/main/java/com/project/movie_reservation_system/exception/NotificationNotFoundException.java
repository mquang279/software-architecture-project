package com.project.movie_reservation_system.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException() {
        super("Cannot found this notification");
    }
}
