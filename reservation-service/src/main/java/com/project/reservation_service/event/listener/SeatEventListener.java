package com.project.reservation_service.event.listener;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.reservation_service.event.model.SeatLockFailedEvent;
import com.project.reservation_service.event.model.SeatsLockedEvent;
import com.project.reservation_service.service.ReservationService;

@Component
@KafkaListener(topics = "events.seat", groupId = "reservation-service")
public class SeatEventListener {
    private final ObjectMapper mapper;
    private final ReservationService reservationService;

    public SeatEventListener(ObjectMapper mapper, ReservationService reservationService) {
        this.mapper = mapper;
        this.reservationService = reservationService;
    }

    @KafkaHandler
    public void handleSeatLockEvent(String message) {
        try {
            JsonNode root = mapper.readTree(message);

            JsonNode envelope = root.get("payload");

            String eventType = envelope.get("type").asText();

            JsonNode businessDataNode = envelope.get("payload");

            if (eventType.equals("SEAT_LOCKED")) {
                SeatsLockedEvent event = mapper.treeToValue(businessDataNode, SeatsLockedEvent.class);
                this.reservationService.handleSeatsStatus(event.getReservationId(), eventType);
            } else {
                SeatLockFailedEvent event = mapper.treeToValue(businessDataNode, SeatLockFailedEvent.class);
                this.reservationService.handleSeatsStatus(event.getReservationId(), eventType);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
