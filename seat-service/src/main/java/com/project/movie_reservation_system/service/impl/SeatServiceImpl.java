package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.dto.PaginationResponse;
import com.project.movie_reservation_system.entity.Seat;
import com.project.movie_reservation_system.enums.SeatStatus;
import com.project.movie_reservation_system.exception.SeatAlreadyBookedException;
import com.project.movie_reservation_system.exception.SeatLockAcquiredException;
import com.project.movie_reservation_system.exception.SeatNotFoundException;
import com.project.movie_reservation_system.repository.SeatRepository;
import com.project.movie_reservation_system.service.SeatLockManager;
import com.project.movie_reservation_system.service.SeatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Service
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;
    private final SeatLockManager seatLockManager;

    public SeatServiceImpl(SeatRepository seatRepository, SeatLockManager seatLockManager) {
        this.seatRepository = seatRepository;
        this.seatLockManager = seatLockManager;
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
     * Lock ghế và đánh dấu là LOCKED
     * Giữ lock trong SeatLockManager cho đến khi gọi unlockSeats()
     */
    public void lockSeats(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            ReentrantLock lock = seatLockManager.getLockForSeat(seatId);
            boolean acquired = lock.tryLock();

            if (!acquired) {

                releasePreviousLocks(seatIds, seatId);
                throw new SeatLockAcquiredException();
            }

            try {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new SeatNotFoundException(seatId));

                if (seat.getStatus() != SeatStatus.UNBOOKED) {
                    releasePreviousLocks(seatIds, seatId);
                    lock.unlock();
                    throw new SeatAlreadyBookedException();
                }

                seat.setStatus(SeatStatus.LOCKED);
                seatRepository.save(seat);

            } catch (Exception e) {
                lock.unlock();
                throw e;
            }
        }
        // Tất cả locks vẫn đang được giữ trong SeatLockManager
    }

    /**
     * Release locks của các ghế đã lock trước seatId
     */
    private void releasePreviousLocks(List<Long> seatIds, Long currentSeatId) {
        for (Long seatId : seatIds) {
            if (seatId.equals(currentSeatId)) {
                break;
            }
            ReentrantLock lock = seatLockManager.getLockForSeat(seatId);
            if (lock.isHeldByCurrentThread()) {
                // Trả ghế về UNBOOKED
                seatRepository.findById(seatId).ifPresent(seat -> {
                    if (seat.getStatus() == SeatStatus.LOCKED) {
                        seat.setStatus(SeatStatus.UNBOOKED);
                        seatRepository.save(seat);
                    }
                });
                lock.unlock();
                seatLockManager.removeLockForSeat(seatId);
            }
        }
    }

    /**
     * Unlock ghế và xóa lock khỏi manager
     * Nếu ghế đang LOCKED, trả về UNBOOKED
     * Nếu ghế đã BOOKED, giữ nguyên
     */
    public void unlockSeats(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            ReentrantLock lock = seatLockManager.getLockForSeat(seatId);

            if (lock.isHeldByCurrentThread()) {
                try {
                    seatRepository.findById(seatId).ifPresent(seat -> {
                        if (seat.getStatus() == SeatStatus.LOCKED) {
                            seat.setStatus(SeatStatus.UNBOOKED);
                            seatRepository.save(seat);
                        }
                    });
                } finally {
                    lock.unlock();
                    seatLockManager.removeLockForSeat(seatId);
                }
            }
        }
    }

    /**
     * Cập nhật trạng thái ghế (LOCKED -> BOOKED hoặc BOOKED -> UNBOOKED)
     */
    public void updateSeatStatus(List<Long> seatIds, String status) {
        SeatStatus seatStatus;
        try {
            seatStatus = SeatStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid seat status: " + status);
        }

        for (Long seatId : seatIds) {
            ReentrantLock lock = seatLockManager.getLockForSeat(seatId);

            // Nếu thread hiện tại đang giữ lock, không cần lock lại
            boolean needToLock = !lock.isHeldByCurrentThread();

            if (needToLock) {
                lock.lock();
            }

            try {
                Seat seat = getSeatById(seatId);

                // Validate transition
                if (seatStatus == SeatStatus.BOOKED && seat.getStatus() != SeatStatus.LOCKED) {
                    throw new RuntimeException(
                            "Cannot book seat " + seatId + " that is not locked");
                }

                seat.setStatus(seatStatus);
                seatRepository.save(seat);

            } finally {
                if (needToLock) {
                    lock.unlock();
                }
            }
        }
    }
}