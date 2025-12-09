package com.project.reservation_service.event.listener;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@KafkaListener(topics = "events.payment", groupId = "reservation-service")
public class SeatEventListener {
    private final ObjectMapper mapper;

    public SeatEventListener(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaHandler
    public void handlePaymentEvent(String message) {
        try {
            JsonNode rootNode = mapper.readTree(message);

            System.out.println(message);

            String payloadString = rootNode.has("payload")
                    ? rootNode.get("payload").asText()
                    : message;

            String aggregatedType = rootNode.get("payload").asText();

            // ReservationCreatedEvent event = mapper.readValue(payloadString,
            // ReservationCreatedEvent.class);

            // System.out.println("Processing lock for reservation: " +
            // event.getReservationId());

            // seatService.processSeatLocking(event.getReservationId(), event.getUserId(),
            // event.getSeatIds());

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
