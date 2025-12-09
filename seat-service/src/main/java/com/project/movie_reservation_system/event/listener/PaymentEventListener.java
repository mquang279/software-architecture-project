package com.project.movie_reservation_system.event.listener;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.movie_reservation_system.event.model.PaymentFailEvent;
import com.project.movie_reservation_system.event.model.PaymentSuccessEvent;
import com.project.movie_reservation_system.service.SeatService;

@Component
@KafkaListener(topics = "events.payment", groupId = "seat-service")
public class PaymentEventListener {
    private final SeatService seatService;
    private final ObjectMapper mapper;

    public PaymentEventListener(SeatService seatService, ObjectMapper mapper) {
        this.seatService = seatService;
        this.mapper = mapper;
    }

    @KafkaHandler
    public void handleReservationEvent(String message) {
        try {
            JsonNode root = mapper.readTree(message);

            JsonNode envelope = root.get("payload");

            String eventType = envelope.get("type").asText();
            String aggregateId = envelope.get("aggregate_id").asText();
            String createdAt = envelope.get("created_at").asText();

            JsonNode businessDataNode = envelope.get("payload");

            System.out.println("Handle Payment in seat-service");
            if (eventType.equals("PAYMENT_SUCCESS")) {
                PaymentSuccessEvent event = mapper.treeToValue(businessDataNode, PaymentSuccessEvent.class);
                this.seatService.handlePaymentStatus(event.getReservationId(), event.getSeatIds(), event.getStatus());
            } else if (eventType.equals("PAYMENT_FAIL")) {
                PaymentFailEvent event = mapper.treeToValue(businessDataNode, PaymentFailEvent.class);
                this.seatService.handlePaymentStatus(event.getReservationId(), event.getSeatIds(), event.getStatus());
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
