package com.saga.orchestrator.entity;

import com.saga.orchestrator.saga.state.SagaStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ReservationSagaState {
    @Id
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    private String currentStep;
}
