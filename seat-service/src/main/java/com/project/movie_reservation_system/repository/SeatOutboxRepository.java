package com.project.movie_reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.movie_reservation_system.entity.SeatOutbox;

@Repository
public interface SeatOutboxRepository extends JpaRepository<SeatOutbox, Long>{
    
}
