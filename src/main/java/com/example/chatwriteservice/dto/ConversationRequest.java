package com.example.chatwriteservice.dto;

import com.example.chatwriteservice.entity.ConversationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {
    
    private String title;
    
    @NotNull(message = "Conversation type is required")
    private ConversationType type;
    
    private String metadata;
    
    @NotNull(message = "Participant emails are required")
    private List<String> participantEmails;
}
