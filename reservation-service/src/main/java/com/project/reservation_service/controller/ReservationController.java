package com.project.reservation_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.project.reservation_service.dto.ApiResponse;
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
//        String currentUserName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PaginationResponse<Reservation> paginationResponse = reservationService
                .getAllReservationsForUser(userId, page, size);

        return ResponseEntity.ok(paginationResponse);
    }

    @Secured({ "ROLE_ADMIN", "ROLE_SUPER_ADMIN" })
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
    public ResponseEntity<ApiResponse<Reservation>> createReservation(
            @RequestBody ReservationRequestDto reservationRequestDto, Long userId) {
//        String currentUserName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reservation reservation = reservationService.createReservation(reservationRequestDto, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation created with id: " + reservation.getId(), reservation));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Reservation>> getReservationById(@PathVariable long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(
                ApiResponse.success("Reservation fetched with id: " + reservation.getId(), reservation));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Reservation>> cancelReservation(@PathVariable long reservationId) {
        Reservation reservation = reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(
                ApiResponse.success("Reservation canceled", reservation));
    }

}
