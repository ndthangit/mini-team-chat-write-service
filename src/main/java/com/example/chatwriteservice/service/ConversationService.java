package com.example.chatwriteservice.service;

import com.example.chatwriteservice.dto.ConversationRequest;
import com.example.chatwriteservice.dto.ConversationResponse;
import com.example.chatwriteservice.entity.Conversation;
import com.example.chatwriteservice.entity.Participant;
import com.example.chatwriteservice.entity.User;
import com.example.chatwriteservice.exception.BadRequestException;
import com.example.chatwriteservice.exception.ResourceNotFoundException;
import com.example.chatwriteservice.repository.ConversationRepository;
import com.example.chatwriteservice.repository.ParticipantRepository;
import com.example.chatwriteservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConversationResponse createConversation(ConversationRequest request) {
        // Create conversation
        Conversation conversation = Conversation.builder()
                .title(request.getTitle())
                .type(request.getType())
                .metadata(request.getMetadata())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        conversation = conversationRepository.save(conversation);

        // Add participants
        for (String email : request.getParticipantEmails()) {
            User user = userRepository.findById(email)
                    .orElseGet(() -> {
                        // Create user if not exists
                        User newUser = User.builder()
                                .email(email)
                                .createdAt(LocalDateTime.now())
                                .build();
                        return userRepository.save(newUser);
                    });

            Participant participant = Participant.builder()
                    .conversation(conversation)
                    .user(user)
                    .joinedAt(LocalDateTime.now())
                    .build();

            participantRepository.save(participant);
        }

        log.info("Conversation created: {}", conversation.getId());

        return mapToResponse(conversation);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(UUID id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        return mapToResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsByUserEmail(String email) {
        List<Conversation> conversations = conversationRepository.findByUserEmail(email);
        return conversations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteConversation(UUID id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        // Delete all participants
        List<Participant> participants = participantRepository.findByConversationId(id);
        participantRepository.deleteAll(participants);
        
        // Delete conversation
        conversationRepository.delete(conversation);
        
        log.info("Conversation deleted: {}", id);
    }

    @Transactional
    public ConversationResponse addParticipant(UUID conversationId, String email) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Check if already participant
        if (participantRepository.findByConversationIdAndEmail(conversationId, email) != null) {
            throw new BadRequestException("User is already a participant");
        }

        User user = userRepository.findById(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });

        Participant participant = Participant.builder()
                .conversation(conversation)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();

        participantRepository.save(participant);
        
        log.info("Participant added to conversation: {} - {}", conversationId, email);

        return mapToResponse(conversation);
    }

    @Transactional
    public ConversationResponse removeParticipant(UUID conversationId, String email) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        Participant participant = participantRepository.findByConversationIdAndEmail(conversationId, email);
        if (participant == null) {
            throw new BadRequestException("User is not a participant");
        }

        participantRepository.delete(participant);
        
        log.info("Participant removed from conversation: {} - {}", conversationId, email);

        return mapToResponse(conversation);
    }

    private ConversationResponse mapToResponse(Conversation conversation) {
        List<String> participantEmails = participantRepository.findByConversationId(conversation.getId())
                .stream()
                .map(p -> p.getUser().getEmail())
                .collect(Collectors.toList());

        return ConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .type(conversation.getType())
                .metadata(conversation.getMetadata())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .participantEmails(participantEmails)
                .build();
    }
}
