package com.project.movie_reservation_system.exception;

public class SeatLockAcquiredException extends RuntimeException {

    public SeatLockAcquiredException() {
        super("Failed to acquire lock on seat. The seat may already be locked by another user.");
    }
}
