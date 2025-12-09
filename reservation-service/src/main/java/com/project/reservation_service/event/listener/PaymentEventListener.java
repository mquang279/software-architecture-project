package com.project.reservation_service.event.listener;

// import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
// @KafkaListener(topics = "events.payment", groupId = "reservation-service")
public class PaymentEventListener {
    private final ObjectMapper mapper;

    public PaymentEventListener(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}
