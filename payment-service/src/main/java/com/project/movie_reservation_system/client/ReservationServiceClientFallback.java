package com.project.movie_reservation_system.client;

import com.project.movie_reservation_system.exception.ReservationServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class ReservationServiceClientFallback implements ReservationServiceClient {
    @Override
    public void confirmReservation(Long reservationId) {
        throw new ReservationServiceUnavailableException(
                reservationId,
                "Cannot confirm reservation - Reservation service is temporarily unavailable. Payment will be refunded."
        );
    }

    @Override
    public void cancelReservationByPayment(Long reservationId) {
        throw new ReservationServiceUnavailableException(
                reservationId,
                "Cannot cancel reservation - Reservation service is temporarily unavailable. Manual intervention required."
        );
    }
}
