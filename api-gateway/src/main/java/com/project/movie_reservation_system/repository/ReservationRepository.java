package com.project.movie_reservation_system.repository;

import com.project.movie_reservation_system.entity.Reservation;
import com.project.movie_reservation_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByUser(User user, Pageable pageable);
}
