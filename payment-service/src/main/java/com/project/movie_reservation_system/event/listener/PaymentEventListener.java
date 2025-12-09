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
public class PaymentEventListener {
    private final PaymentService paymentService;
    private final ObjectMapper mapper;

    public PaymentEventListener(PaymentService paymentService, ObjectMapper mapper) {
        this.paymentService = paymentService;
        this.mapper = mapper;
    }

    @KafkaHandler
    public void handlePayment(String message) {
        try {
            JsonNode rootNode = mapper.readTree(message);

            String payloadString = rootNode.has("payload")
                    ? rootNode.get("payload").asText()
                    : message;

            SeatLockedEvent event = mapper.readValue(payloadString, SeatLockedEvent.class);

            System.out.println("Handle payment: " + event);

            paymentService.processPaymentForReservation(event.getReservationId(), event.getUserId(),
                    event.getTotalPrice(), event.getSeatIds());

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
