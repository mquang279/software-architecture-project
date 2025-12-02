package com.project.movie_reservation_system.repository;

import com.project.movie_reservation_system.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    Page<Show> findByMovieId(long movieId, Pageable pageable);
    Page<Show> findByMovieIdAndStartTimeBetween(Long movieId, Instant from, Instant to, Pageable pageable);
}
