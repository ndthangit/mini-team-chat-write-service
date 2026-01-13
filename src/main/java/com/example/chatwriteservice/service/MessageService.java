package com.example.chatwriteservice.service;

import com.example.chatwriteservice.dto.MessageRequest;
import com.example.chatwriteservice.dto.MessageResponse;
import com.example.chatwriteservice.entity.Conversation;
import com.example.chatwriteservice.entity.Message;
import com.example.chatwriteservice.entity.User;
import com.example.chatwriteservice.exception.BadRequestException;
import com.example.chatwriteservice.exception.ResourceNotFoundException;
import com.example.chatwriteservice.repository.ConversationRepository;
import com.example.chatwriteservice.repository.MessageRepository;
import com.example.chatwriteservice.repository.ParticipantRepository;
import com.example.chatwriteservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public MessageResponse sendMessage(MessageRequest request) {
        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Validate user exists
        User sender = userRepository.findById(request.getSenderEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate user is participant
        if (participantRepository.findByConversationIdAndEmail(
                request.getConversationId(), request.getSenderEmail()) == null) {
            throw new BadRequestException("User is not a participant of this conversation");
        }

        // Create and save message
        Message message = Message.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .sender(sender)
                .type(request.getType())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        message = messageRepository.save(message);

        // Update conversation updated_at
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.info("Message sent: {} in conversation: {}", message.getId(), conversation.getId());

        return mapToResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesByConversationId(UUID conversationId, Pageable pageable) {
        Page<Message> messages = messageRepository.findByConversationIdAndNotDeleted(conversationId, pageable);
        return messages.map(this::mapToResponse);
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderEmail(message.getSender().getEmail())
                .type(message.getType())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isDeleted(message.getIsDeleted())
                .build();
    }
}
