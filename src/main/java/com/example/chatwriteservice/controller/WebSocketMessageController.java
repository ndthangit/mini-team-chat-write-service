package com.example.chatwriteservice.controller;

import com.example.chatwriteservice.dto.MessageRequest;
import com.example.chatwriteservice.dto.MessageResponse;
import com.example.chatwriteservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming messages from clients
     * Client sends to: /app/chat.sendMessage
     * Server broadcasts to: /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload @Valid MessageRequest request) {
        try {
            log.info("Received message from: {} for conversation: {}", 
                    request.getSenderEmail(), request.getConversationId());
            
            // Save message to database
            MessageResponse response = messageService.sendMessage(request);
            
            // Broadcast message to all subscribers of the conversation
            String destination = "/topic/conversation/" + request.getConversationId();
            messagingTemplate.convertAndSend(destination, response);
            
            log.info("Message broadcasted to: {}", destination);
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            // Send error to sender
            messagingTemplate.convertAndSendToUser(
                    request.getSenderEmail(),
                    "/queue/errors",
                    "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat.typing
     * Server broadcasts to: /topic/conversation/{conversationId}/typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicator indicator) {
        String destination = "/topic/conversation/" + indicator.getConversationId() + "/typing";
        messagingTemplate.convertAndSend(destination, indicator);
    }

    // DTO for typing indicator
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TypingIndicator {
        private String conversationId;
        private String userEmail;
        private boolean isTyping;

    }
}
