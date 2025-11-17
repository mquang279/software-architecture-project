package com.saga.orchestrator.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    @KafkaListener(topics = "dbserver1.public.reservation", groupId = "orchestrator")
    public void listen(String message) {
        System.out.println("Received Message: " + message);
    }
}
