package com.example.chatwriteservice.repository;

import com.example.chatwriteservice.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    
    @Query("SELECT DISTINCT c FROM Conversation c JOIN Participant p ON c.id = p.conversation.id WHERE p.user.email = :email ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserEmail(@Param("email") String email);
}
