package com.project.movie_reservation_system.event.listener;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.movie_reservation_system.event.model.SeatLockedEvent;
import com.project.movie_reservation_system.service.PaymentService;

@Component
@KafkaListener(topics = "events.seat", groupId = "payment-service")
public class SeatEventListener {
    private final PaymentService paymentService;
    private final ObjectMapper mapper;

    public SeatEventListener(PaymentService paymentService, ObjectMapper mapper) {
        this.paymentService = paymentService;
        this.mapper = mapper;
    }

    @KafkaHandler
    public void handlePayment(String message) {
        try {

            JsonNode root = mapper.readTree(message);

            JsonNode envelope = root.get("payload");

            String eventType = envelope.get("type").asText();
            String aggregateId = envelope.get("aggregate_id").asText();
            String createdAt = envelope.get("created_at").asText();

            JsonNode businessDataNode = envelope.get("payload");

            SeatLockedEvent event = mapper.treeToValue(businessDataNode, SeatLockedEvent.class);

            paymentService.processPaymentForReservation(event.getReservationId(), event.getUserId(),
                    event.getTotalPrice(), event.getSeatIds());

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
