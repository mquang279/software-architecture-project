package com.project.movie_reservation_system.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "reservation-service", fallback = ReservationServiceClientFallback.class)
public interface ReservationServiceClient {

    @PostMapping("/api/v1/reservations/{reservationId}/confirm")
    void confirmReservation(@PathVariable Long reservationId);

    @PostMapping("/api/v1/reservations/{reservationId}/cancel-by-payment")
    void cancelReservationByPayment(@PathVariable Long reservationId);
}