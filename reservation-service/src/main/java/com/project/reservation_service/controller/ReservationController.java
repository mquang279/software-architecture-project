package com.project.reservation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.reservation_service.dto.PaginationResponse;
import com.project.reservation_service.dto.ReservationRequestDto;
import com.project.reservation_service.entity.Reservation;
import com.project.reservation_service.service.ReservationService;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<Reservation>> getAllReservationsForCurrentUser(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationResponse<Reservation> paginationResponse = reservationService
                .getAllReservationsForUser(userId, page, size);
        return ResponseEntity.ok(paginationResponse);
    }

    @GetMapping("/filter")
    public ResponseEntity<PaginationResponse<Reservation>> filterReservations(
            @RequestParam(required = false) long theaterId,
            @RequestParam(required = false) long movieId,
            @RequestParam(required = false) long userId,
            @RequestParam(defaultValue = "BOOKED") String reservationStatus,
            @RequestParam(required = false) String createdDate) {

        return null;
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestBody ReservationRequestDto reservationRequestDto, Long userId) {
        Reservation reservation = reservationService.createReservation(reservationRequestDto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservation);
    }

    @PostMapping("/api/v1/reservations/{reservationId}/confirm")
    public void confirmReservation(@PathVariable Long reservationId) {
        reservationService.confirmReservation(reservationId);
    }


    @PostMapping("/api/v1/reservations/{reservationId}/cancel-by-payment")
    public void cancelReservationByPayment(@PathVariable Long reservationId) {
        reservationService.cancelReservationByPayment(reservationId);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(reservation);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable long reservationId) {
        Reservation reservation = reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(reservation);
    }



}
