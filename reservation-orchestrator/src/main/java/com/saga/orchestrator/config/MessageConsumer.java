package com.saga.orchestrator.config;

import org.springframework.kafka.annotation.KafkaListener;

public class MessageConsumer {
    @KafkaListener(topics = "techmaster", groupId = "techmaster")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}
