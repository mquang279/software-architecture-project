package com.project.movie_reservation_system.exception;

public class ReservationServiceUnavailableException extends RuntimeException {
    private final Long reservationId;

    public ReservationServiceUnavailableException(Long reservationId) {
        super(String.format("Reservation service is temporarily unavailable. Cannot process reservation ID: %d", reservationId));
        this.reservationId = reservationId;
    }

    public ReservationServiceUnavailableException(Long reservationId, String message) {
        super(message);
        this.reservationId = reservationId;
    }

    public Long getReservationId() {
        return reservationId;
    }
}