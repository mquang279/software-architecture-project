package com.saga.orchestrator.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MessageConsumer {
    @KafkaListener(topics = "dbserver1.public.reservation", groupId = "orchestrator")
    public void handleMessage(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(message);
        JsonNode payload = root.get("payload");
        JsonNode after = payload.get("after");
        System.out.println("Payload: " + after);
    }
}
