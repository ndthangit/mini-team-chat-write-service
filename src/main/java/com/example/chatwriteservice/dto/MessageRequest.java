package com.example.chatwriteservice.dto;

import com.example.chatwriteservice.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    
    @NotNull(message = "Conversation ID is required")
    private UUID conversationId;
    
    @NotBlank(message = "Sender email is required")
    private String senderEmail;
    
    @NotNull(message = "Message type is required")
    private MessageType type;
    
    private String content;
}
