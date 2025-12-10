package com.project.reservation_service.event.listener;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.reservation_service.event.model.PaymentFailEvent;
import com.project.reservation_service.event.model.PaymentSuccessEvent;
import com.project.reservation_service.service.ReservationService;

@Component
@KafkaListener(topics = "events.payment", groupId = "reservation-service")
public class PaymentEventListener {
    private final ObjectMapper mapper;
    private final ReservationService reservationService;

    public PaymentEventListener(ObjectMapper mapper, ReservationService reservationService) {
        this.mapper = mapper;
        this.reservationService = reservationService;
    }

    @KafkaHandler
    public void handlePaymentEvent(String message) {
        try {
            JsonNode root = mapper.readTree(message);

            JsonNode envelope = root.get("payload");

            String eventType = envelope.get("type").asText();

            JsonNode businessDataNode = envelope.get("payload");

            if (eventType.equals("PAYMENT_SUCCESS")) {
                PaymentSuccessEvent event = mapper.treeToValue(businessDataNode, PaymentSuccessEvent.class);
                this.reservationService.handlePaymentStatus(event.getReservationId(), event.getStatus());
            } else {
                PaymentFailEvent event = mapper.treeToValue(businessDataNode, PaymentFailEvent.class);
                this.reservationService.handlePaymentStatus(event.getReservationId(), event.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
