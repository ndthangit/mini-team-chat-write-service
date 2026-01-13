package com.example.chatwriteservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(id = "myId", topics = "team-events")
    public void listen(String message) {
        System.out.println("<<< Nhận được tin nhắn: " + message);
    }

}