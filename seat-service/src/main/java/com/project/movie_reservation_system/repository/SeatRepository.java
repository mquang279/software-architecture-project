package com.project.movie_reservation_system.repository;

import com.project.movie_reservation_system.entity.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    Page<Seat> findByShowId(Long showId, Pageable pageable);
}
