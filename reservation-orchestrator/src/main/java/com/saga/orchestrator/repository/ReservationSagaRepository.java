package com.saga.orchestrator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saga.orchestrator.entity.ReservationSagaState;

@Repository
public interface ReservationSagaRepository extends JpaRepository<ReservationSagaState, Long> {
    
}
