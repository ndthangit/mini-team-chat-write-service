package com.example.chatwriteservice.controller;

import com.example.chatwriteservice.dto.ConversationRequest;
import com.example.chatwriteservice.dto.ConversationResponse;
import com.example.chatwriteservice.dto.MessageResponse;
import com.example.chatwriteservice.service.ConversationService;
import com.example.chatwriteservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    /**
     * Create a new conversation
     */
    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody ConversationRequest request) {
        ConversationResponse response = conversationService.createConversation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get conversation by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable UUID id) {
        ConversationResponse response = conversationService.getConversationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all conversations for a user
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<List<ConversationResponse>> getConversationsByUser(
            @PathVariable String email) {
        List<ConversationResponse> responses = conversationService.getConversationsByUserEmail(email);
        return ResponseEntity.ok(responses);
    }

    /**
     * Delete a conversation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a participant to conversation
     */
    @PostMapping("/{id}/participants")
    public ResponseEntity<ConversationResponse> addParticipant(
            @PathVariable UUID id,
            @RequestParam String email) {
        ConversationResponse response = conversationService.addParticipant(id, email);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a participant from conversation
     */
    @DeleteMapping("/{id}/participants/{email}")
    public ResponseEntity<ConversationResponse> removeParticipant(
            @PathVariable UUID id,
            @PathVariable String email) {
        ConversationResponse response = conversationService.removeParticipant(id, email);
        return ResponseEntity.ok(response);
    }

    /**
     * Get messages for a conversation with pagination
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> messages = messageService.getMessagesByConversationId(id, pageable);
        return ResponseEntity.ok(messages);
    }
}
