package com.example.chatwriteservice.repository;

import com.example.chatwriteservice.entity.Message;
import com.example.chatwriteservice.entity.MessageId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, MessageId> {
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdAndNotDeleted(@Param("conversationId") UUID conversationId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdAndNotDeleted(@Param("conversationId") UUID conversationId);
}
