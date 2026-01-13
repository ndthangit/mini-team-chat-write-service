package com.example.chatwriteservice.service;

import com.example.chatwriteservice.dto.ConversationRequest;
import com.example.chatwriteservice.dto.ConversationResponse;
import com.example.chatwriteservice.entity.Conversation;
import com.example.chatwriteservice.entity.ConversationType;
import com.example.chatwriteservice.entity.Participant;
import com.example.chatwriteservice.entity.User;
import com.example.chatwriteservice.exception.BadRequestException;
import com.example.chatwriteservice.exception.ResourceNotFoundException;
import com.example.chatwriteservice.repository.ConversationRepository;
import com.example.chatwriteservice.repository.ParticipantRepository;
import com.example.chatwriteservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConversationService conversationService;

    private Conversation testConversation;
    private User testUser1;
    private User testUser2;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        conversationId = UUID.randomUUID();
        
        testUser1 = User.builder()
                .email("user1@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .email("user2@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        testConversation = Conversation.builder()
                .id(conversationId)
                .title("Test Conversation")
                .type(ConversationType.PRIVATE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createConversation_ShouldCreateSuccessfully() {
        // Arrange
        ConversationRequest request = ConversationRequest.builder()
                .title("New Chat")
                .type(ConversationType.PRIVATE)
                .participantEmails(Arrays.asList("user1@example.com", "user2@example.com"))
                .build();

        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
        when(userRepository.findById("user1@example.com")).thenReturn(Optional.of(testUser1));
        when(userRepository.findById("user2@example.com")).thenReturn(Optional.of(testUser2));
        when(participantRepository.findByConversationId(any())).thenReturn(
                Arrays.asList(
                        createParticipant(testConversation, testUser1),
                        createParticipant(testConversation, testUser2)
                )
        );

        // Act
        ConversationResponse response = conversationService.createConversation(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getParticipantEmails().size());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(participantRepository, times(2)).save(any(Participant.class));
    }

    @Test
    void createConversation_ShouldCreateNewUsersIfNotExist() {
        // Arrange
        ConversationRequest request = ConversationRequest.builder()
                .title("New Chat")
                .type(ConversationType.GROUP)
                .participantEmails(Arrays.asList("newuser@example.com"))
                .build();

        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
        when(userRepository.findById("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser1);
        when(participantRepository.findByConversationId(any())).thenReturn(Collections.emptyList());

        // Act
        ConversationResponse response = conversationService.createConversation(request);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getConversationById_ShouldReturnConversation() {
        // Arrange
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(participantRepository.findByConversationId(conversationId)).thenReturn(Collections.emptyList());

        // Act
        ConversationResponse response = conversationService.getConversationById(conversationId);

        // Assert
        assertNotNull(response);
        assertEquals(conversationId, response.getId());
        assertEquals("Test Conversation", response.getTitle());
    }

    @Test
    void getConversationById_ShouldThrowNotFoundException() {
        // Arrange
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            conversationService.getConversationById(conversationId);
        });
    }

    @Test
    void addParticipant_ShouldAddSuccessfully() {
        // Arrange
        String newEmail = "newuser@example.com";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(participantRepository.findByConversationIdAndEmail(conversationId, newEmail)).thenReturn(null);
        when(userRepository.findById(newEmail)).thenReturn(Optional.of(testUser1));
        when(participantRepository.findByConversationId(conversationId)).thenReturn(Collections.emptyList());

        // Act
        ConversationResponse response = conversationService.addParticipant(conversationId, newEmail);

        // Assert
        assertNotNull(response);
        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @Test
    void addParticipant_ShouldThrowExceptionIfAlreadyParticipant() {
        // Arrange
        String email = "user1@example.com";
        Participant existingParticipant = createParticipant(testConversation, testUser1);
        
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(participantRepository.findByConversationIdAndEmail(conversationId, email))
                .thenReturn(existingParticipant);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            conversationService.addParticipant(conversationId, email);
        });
    }

    @Test
    void removeParticipant_ShouldRemoveSuccessfully() {
        // Arrange
        String email = "user1@example.com";
        Participant participant = createParticipant(testConversation, testUser1);
        
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(participantRepository.findByConversationIdAndEmail(conversationId, email))
                .thenReturn(participant);
        when(participantRepository.findByConversationId(conversationId)).thenReturn(Collections.emptyList());

        // Act
        ConversationResponse response = conversationService.removeParticipant(conversationId, email);

        // Assert
        assertNotNull(response);
        verify(participantRepository, times(1)).delete(participant);
    }

    @Test
    void deleteConversation_ShouldDeleteSuccessfully() {
        // Arrange
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(participantRepository.findByConversationId(conversationId)).thenReturn(Collections.emptyList());

        // Act
        conversationService.deleteConversation(conversationId);

        // Assert
        verify(conversationRepository, times(1)).delete(testConversation);
        verify(participantRepository, times(1)).deleteAll(any());
    }

    private Participant createParticipant(Conversation conversation, User user) {
        return Participant.builder()
                .id(1L)
                .conversation(conversation)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
