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
public class SeatEventListener {
    private final SeatService seatService;
    private final ObjectMapper mapper;

    public SeatEventListener(SeatService seatService, ObjectMapper mapper) {
        this.seatService = seatService;
        this.mapper = mapper;
    }

    @KafkaHandler
    public void lockSeats(String message) {
        try {
            JsonNode rootNode = mapper.readTree(message);

            String payloadString = rootNode.has("payload")
                    ? rootNode.get("payload").asText()
                    : message;

            ReservationCreatedEvent event = mapper.readValue(payloadString, ReservationCreatedEvent.class);

            System.out.println("Processing lock for reservation: " + event.getReservationId());

            seatService.processSeatLocking(event.getReservationId(), event.getUserId(), event.getSeatIds());

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
