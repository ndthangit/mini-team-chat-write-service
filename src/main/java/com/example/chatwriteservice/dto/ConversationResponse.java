package com.example.chatwriteservice.dto;

import com.example.chatwriteservice.entity.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private UUID id;
    private String title;
    private ConversationType type;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> participantEmails;
}
