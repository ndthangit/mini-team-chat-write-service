package com.example.chatwriteservice.repository;

import com.example.chatwriteservice.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    
    @Query("SELECT p FROM Participant p WHERE p.conversation.id = :conversationId")
    List<Participant> findByConversationId(@Param("conversationId") UUID conversationId);
    
    @Query("SELECT p FROM Participant p WHERE p.conversation.id = :conversationId AND p.user.email = :email")
    Participant findByConversationIdAndEmail(@Param("conversationId") UUID conversationId, @Param("email") String email);
}
