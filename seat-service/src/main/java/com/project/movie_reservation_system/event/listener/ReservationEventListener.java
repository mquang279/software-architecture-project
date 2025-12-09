package com.project.movie_reservation_system.event.listener;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.movie_reservation_system.event.model.ReservationCreatedEvent;
import com.project.movie_reservation_system.service.SeatService;

@Component
@KafkaListener(topics = "events.reservation", groupId = "seat-service")
public class ReservationEventListener {
    private final SeatService seatService;
    private final ObjectMapper mapper;

    public ReservationEventListener(SeatService seatService, ObjectMapper mapper) {
        this.seatService = seatService;
        this.mapper = mapper;
    }

    @KafkaHandler
    public void handleReservationEvent(String message) {
        try {
            System.out.println(message);
            JsonNode root = mapper.readTree(message);

            JsonNode envelope = root.get("payload");

            String eventType = envelope.get("type").asText();
            String aggregateId = envelope.get("aggregate_id").asText();
            String createdAt = envelope.get("created_at").asText();

            JsonNode businessDataNode = envelope.get("payload");
            ReservationCreatedEvent event = mapper.treeToValue(businessDataNode, ReservationCreatedEvent.class);

            System.out.println("Processing lock for reservation: " + event.getReservationId());

            seatService.processSeatLocking(event.getReservationId(), event.getUserId(), event.getSeatIds());

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
