package com.project.movie_reservation_system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Outbox;
import com.project.movie_reservation_system.entity.Seat;
import com.project.movie_reservation_system.enums.PaymentStatus;
import com.project.movie_reservation_system.enums.SeatStatus;
import com.project.movie_reservation_system.event.model.SeatLockFailedEvent;
import com.project.movie_reservation_system.event.model.SeatsLockedEvent;
import com.project.movie_reservation_system.exception.SeatNotFoundException;
import com.project.movie_reservation_system.repository.OutboxRepository;
import com.project.movie_reservation_system.repository.SeatRepository;
import com.project.movie_reservation_system.service.SeatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class SeatServiceImpl implements SeatService {

    private final OutboxRepository outboxRepository;
    private final SeatRepository seatRepository;
    private final ObjectMapper mapper;

    public SeatServiceImpl(SeatRepository seatRepository,
            ObjectMapper objectMapper, OutboxRepository seatOutboxRepository) {
        this.seatRepository = seatRepository;
        this.mapper = objectMapper;
        this.outboxRepository = seatOutboxRepository;
    }

    public List<Seat> createSeatsWithGivenPrice(Long showId, int seats, double price, String area) {
        return IntStream.range(1, seats + 1)
                .mapToObj(seatCount -> Seat.builder()
                        .showId(showId)
                        .price(price)
                        .number(seatCount)
                        .area(area)
                        .status(SeatStatus.UNBOOKED)
                        .build())
                .map(seatRepository::save)
                .toList();
    }

    public Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));
    }

    public PaginationResponse<Seat> getSeatsByShowId(Long showId, int size, int page) {
        Page<Seat> seats = seatRepository.findByShowId(showId, PageRequest.of(page, size));
        return PaginationResponse.<Seat>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(seats.getTotalPages())
                .totalElements(seats.getTotalElements())
                .data(seats.getContent())
                .build();
    }

    public boolean areAllSeatsUnbooked(List<Long> seatIds) {
        List<Seat> seats = seatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new SeatNotFoundException(0L);
        }

        return seats.stream()
                .allMatch(seat -> seat.getStatus() == SeatStatus.UNBOOKED);
    }

    @Transactional
    @Override
    public void processSeatLocking(Long reservationId, Long userId, List<Long> seatIds) {
        try {
            List<Seat> seats = seatRepository.findAllByIdWithLock(seatIds);

            if (seats.size() != seatIds.size()) {
                saveOutboxEvent(reservationId, "SEAT_LOCK_FAILED",
                        new SeatLockFailedEvent(reservationId, "One or more seats not found"));
                return;
            }

            for (Seat seat : seats) {
                if (seat.getStatus() != SeatStatus.UNBOOKED) {
                    saveOutboxEvent(reservationId, "SEAT_LOCK_FAILED",
                            new SeatLockFailedEvent(reservationId, "Seat " + seat.getId() + " is already taken"));
                    return;
                }
            }

            double totalPrice = 0;

            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.LOCKED);
                totalPrice += seat.getPrice();
            }
            seatRepository.saveAll(seats);

            saveOutboxEvent(reservationId, "SEATS_LOCKED",
                    new SeatsLockedEvent(reservationId, userId, totalPrice, seatIds));

        } catch (Exception e) {
            saveOutboxEvent(reservationId, "SEAT_LOCK_FAILED",
                    new SeatLockFailedEvent(reservationId, "System error: " + e.getMessage()));
            throw e;
        }
    }

    private void saveOutboxEvent(Long reservationId, String eventType, Object object) {
        try {
            String payload = mapper.writeValueAsString(object);
            Outbox event = Outbox.builder()
                    .aggregateType("seat")
                    .aggregateId(String.valueOf(reservationId))
                    .type(eventType)
                    .payload(payload)
                    .build();
            outboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize event payload", e);
        }
    }

    @Override
    public void handlePaymentStatus(Long reservationId, List<Long> seatIds, PaymentStatus status) {
        for (Long seatId : seatIds) {
            Seat seat = this.seatRepository.findById(seatId).get();
            if (status == PaymentStatus.SUCCESS) {
                seat.setStatus(SeatStatus.BOOKED);
                seat.setReservationId(reservationId);
            } else {
                seat.setStatus(SeatStatus.UNBOOKED);
            }
            this.seatRepository.save(seat);
        }
    }
}