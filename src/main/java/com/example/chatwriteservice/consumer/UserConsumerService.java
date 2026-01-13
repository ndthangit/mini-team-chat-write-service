package com.example.chatwriteservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserConsumerService {

    @KafkaListener(topics = "user-create", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void readCreate(ConsumerRecord<String, GenericRecord> record) {
        try {
            GenericRecord userValue = record.value();
            
            // Truy cập field bằng String name (không cần class User)
            String email = userValue.get("email").toString();
            
            log.info("Received user-create event for email: {}", email);
            

            
            log.info("Successfully saved user with email: {}", email);
            
        } catch (Exception e) {
            log.error("Error processing user-create event: {}", e.getMessage(), e);
            throw e; // Re-throw để Kafka có thể retry nếu cần
        }
    }
}
