package com.project.reservation_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.reservation_service.dto.*;
import com.project.reservation_service.enums.PaymentStatus;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.reservation_service.client.ShowServiceClient;
import com.project.reservation_service.client.UserServiceClient;
import com.project.reservation_service.entity.Outbox;
import com.project.reservation_service.entity.Reservation;
import com.project.reservation_service.enums.ReservationStatus;
import com.project.reservation_service.exception.*;
import com.project.reservation_service.repository.OutboxRepository;
import com.project.reservation_service.repository.ReservationRepository;
import com.project.reservation_service.service.ReservationService;

import java.util.Map;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final OutboxRepository outboxRepository;
    private final UserServiceClient userServiceClient;
    private final ShowServiceClient showServiceClient;
    private final ObjectMapper objectMapper;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserServiceClient userServiceClient,
            ShowServiceClient showServiceClient,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.reservationRepository = reservationRepository;
        this.userServiceClient = userServiceClient;
        this.showServiceClient = showServiceClient;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public Reservation createReservation(ReservationRequestDto dto, Long userId) {
        userServiceClient.getUserById(userId);
        showServiceClient.getShowById(dto.getShowId());

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .showId(dto.getShowId())
                .amountPaid(dto.getAmount())
                .reservationStatus(ReservationStatus.PENDING_SEAT_LOCK)
                .build();

        reservationRepository.save(reservation);

        try {
            Map<String, Object> eventPayload = Map.of(
                    "reservationId", reservation.getId(),
                    "userId", reservation.getUserId(),
                    "showId", reservation.getShowId(),
                    "seatIds", dto.getSeatIdsToReserve(),
                    "amount", reservation.getAmountPaid());

            Outbox outbox = Outbox.builder()
                    .aggregateType("reservation")
                    .aggregateId(String.valueOf(reservation.getId()))
                    .type("RESERVATION_CREATED")
                    .payload(objectMapper.writeValueAsString(eventPayload))
                    .build();

            outboxRepository.save(outbox);

        } catch (Exception ex) {
            System.err.println("Failed to save to outbox: " + ex.getMessage());
        }

        return reservation;
    }

    @Override
    public Reservation getReservationById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

    }

    @Override
    public PaginationResponse<Reservation> getAllReservationsForUser(Long userId, int page, int size) {
        userServiceClient.getUserById(userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reservation> reservationPage = reservationRepository.findByUserId(userId, pageable);

        return PaginationResponse.<Reservation>builder()
                .pageNumber(reservationPage.getNumber())
                .pageSize(reservationPage.getSize())
                .totalPages(reservationPage.getTotalPages())
                .totalElements(reservationPage.getTotalElements())
                .data(reservationPage.getContent())
                .build();
    }

    @Override
    public void handlePaymentStatus(Long reservationId, PaymentStatus status) {
        Reservation reservation = this.reservationRepository.findById(reservationId).get();
        if (status == PaymentStatus.SUCCESS) {
            reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        } else {
            reservation.setReservationStatus(ReservationStatus.CANCELED);
        }
        this.reservationRepository.save(reservation);
    }

    @Override
    public void handleSeatsStatus(Long reservationId, String status) {
        Reservation reservation = this.reservationRepository.findById(reservationId).get();
        if (status.equals("SEATS_LOCKED")) {
            reservation.setReservationStatus(ReservationStatus.SEATS_LOCKED);
        } else {
            reservation.setReservationStatus(ReservationStatus.CANCELED);
        }
        this.reservationRepository.save(reservation);
        throw new UnsupportedOperationException("Unimplemented method 'handleSeatsStatus'");
    }
}
