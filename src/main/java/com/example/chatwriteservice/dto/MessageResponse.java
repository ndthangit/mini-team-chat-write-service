package com.example.chatwriteservice.dto;

import com.example.chatwriteservice.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private UUID id;
    private UUID conversationId;
    private String senderEmail;
    private MessageType type;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
}
