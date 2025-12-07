package com.project.movie_reservation_system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Outbox;
import com.project.movie_reservation_system.entity.Seat;
import com.project.movie_reservation_system.enums.SeatStatus;
import com.project.movie_reservation_system.event.ReservationCreatedEvent;
import com.project.movie_reservation_system.event.SeatLockFailedEvent;
import com.project.movie_reservation_system.event.SeatsLockedEvent;
import com.project.movie_reservation_system.exception.SeatNotFoundException;
import com.project.movie_reservation_system.repository.SeatOutboxRepository;
import com.project.movie_reservation_system.repository.SeatRepository;
import com.project.movie_reservation_system.service.SeatService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Service
public class SeatServiceImpl implements SeatService {

    private final SeatOutboxRepository seatOutboxRepository;
    private final SeatRepository seatRepository;
    private final EntityManager entityManager;
    private final ObjectMapper mapper;

    public SeatServiceImpl(SeatRepository seatRepository,
            EntityManager entityManager, ObjectMapper objectMapper, SeatOutboxRepository seatOutboxRepository) {
        this.seatRepository = seatRepository;
        this.entityManager = entityManager;
        this.mapper = objectMapper;
        this.seatOutboxRepository = seatOutboxRepository;
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

    /**
     * Release locks của các ghế đã lock trước seatId
     */
    // @Transactional
    // public void releasePreviousLocks(List<Long> seatIds, Long currentSeatId) {
    // for (Long seatId : seatIds) {
    // if (seatId.equals(currentSeatId)) {
    // break;
    // }
    // ReentrantLock lock = seatLockManager.getLockForSeat(seatId);
    // if (lock.isHeldByCurrentThread()) {
    // // Trả ghế về UNBOOKED
    // seatRepository.findById(seatId).ifPresent(seat -> {
    // if (seat.getStatus() == SeatStatus.LOCKED) {
    // seat.setStatus(SeatStatus.UNBOOKED);
    // seatRepository.save(seat);
    // }
    // });
    // lock.unlock();
    // seatLockManager.removeLockForSeat(seatId);
    // }
    // }
    // }

    /**
     * Unlock ghế và xóa lock khỏi manager
     * Nếu ghế đang LOCKED, trả về UNBOOKED
     * Nếu ghế đã BOOKED, giữ nguyên
     */
    // @Transactional
    // public void unlockSeats(List<Long> seatIds) {
    // for (Long seatId : seatIds) {
    // ReentrantLock lock = seatLockManager.getLockForSeat(seatId);
    // try {
    // seatRepository.findById(seatId).ifPresent(seat -> {
    // if (seat.getStatus() == SeatStatus.LOCKED) {
    // seat.setStatus(SeatStatus.UNBOOKED);
    // seatRepository.save(seat);
    // entityManager.flush();
    // }
    // });
    // } finally {
    // if (lock.isHeldByCurrentThread()) {
    // lock.unlock();
    // }
    // seatLockManager.removeLockForSeat(seatId);
    // }
    // }
    // }

    /**
     * Cập nhật trạng thái ghế (LOCKED -> BOOKED hoặc BOOKED -> UNBOOKED)
     */
    // @Transactional
    // public void updateSeatStatus(List<Long> seatIds, String status) {
    // SeatStatus seatStatus;
    // try {
    // seatStatus = SeatStatus.valueOf(status.toUpperCase());
    // } catch (IllegalArgumentException e) {
    // throw new RuntimeException("Invalid seat status: " + status);
    // }

    // for (Long seatId : seatIds) {
    // ReentrantLock lock = seatLockManager.getLockForSeat(seatId);

    // // Nếu thread hiện tại đang giữ lock, không cần lock lại
    // boolean needToLock = !lock.isHeldByCurrentThread();

    // if (needToLock) {
    // lock.lock();
    // }

    // try {
    // Seat seat = getSeatById(seatId);

    // // Validate transition
    // if (seatStatus == SeatStatus.BOOKED && seat.getStatus() != SeatStatus.LOCKED)
    // {
    // throw new RuntimeException(
    // "Cannot book seat " + seatId + " that is not locked");
    // }

    // seat.setStatus(seatStatus);
    // seatRepository.save(seat);

    // } finally {
    // if (needToLock) {
    // lock.unlock();
    // }
    // }
    // }
    // }

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
    public void processSeatLocking(Long reservationId, List<Long> seatIds) {
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

            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.LOCKED);
            }
            seatRepository.saveAll(seats);

            saveOutboxEvent(reservationId, "SEATS_LOCKED",
                    new SeatsLockedEvent(reservationId, seatIds));

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
                    .aggregateType("SEAT")
                    .aggregateId(String.valueOf(reservationId))
                    .type(eventType)
                    .payload(payload)
                    .build();
            seatOutboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize event payload", e);
        }
    }
}