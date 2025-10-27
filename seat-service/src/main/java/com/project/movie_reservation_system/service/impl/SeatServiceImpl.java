package com.project.movie_reservation_system.service.impl;

import com.project.movie_reservation_system.entity.Seat;
import com.project.movie_reservation_system.enums.SeatStatus;
import com.project.movie_reservation_system.exception.CustomException;
import com.project.movie_reservation_system.repository.SeatRepository;
import com.project.movie_reservation_system.service.SeatLockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Service
public class SeatServiceImpl {
    private final SeatRepository seatRepository;
    private final SeatLockManager seatLockManager;

    @Autowired
    public SeatServiceImpl(SeatRepository seatRepository, SeatLockManager seatLockManager) {
        this.seatRepository = seatRepository;
        this.seatLockManager = seatLockManager;
    }

    public List<Seat> createSeatsWithGivenPrice(int seats, double price, String area){
        return IntStream.range(1, seats+1)
                .mapToObj(seatCount -> Seat.builder()
                        .price(price)
                        .number(seatCount)
                        .area(area)
                        .status(SeatStatus.UNBOOKED)
                        .build()
                )
                .map(seatRepository::save)
                .toList();
    }

    public Seat getSeatById(Long seatId) {
        Optional<Seat> seat = seatRepository.findById(seatId);
        if (seat.isPresent()) {
            return seat.get();
        } else {
            throw new CustomException("Seat Not Found", HttpStatus.NOT_FOUND);
        }
    }


    public void lockSeats(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            ReentrantLock lock = seatLockManager.getLockForSeat(seatId);
            boolean acquired = lock.tryLock();

            if (!acquired) {
                throw new RuntimeException("Seat " + seatId + " is already locked");
            }

            try {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));

                if (seat.getStatus().equals(SeatStatus.UNBOOKED)) {
                    seat.setStatus(SeatStatus.BOOKED);
                    seatRepository.save(seat);
                } else {
                    throw new RuntimeException("Seat " + seatId + " is not available");
                }
            } finally {
                lock.unlock();
            }
        }
    }





}
