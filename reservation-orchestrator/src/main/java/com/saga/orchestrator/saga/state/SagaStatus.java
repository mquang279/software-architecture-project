package com.saga.orchestrator.saga.state;

public enum SagaStatus {
    STARTED,
    SEAT_LOCKED,
    PAYMENT_COMPLETED,
    COMPLETED,
    FAILED
}
