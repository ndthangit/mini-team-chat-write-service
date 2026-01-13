package com.example.chatwriteservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MessageId.class)
public class Message {
    @Id
    private UUID id;

    @Id
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_email", referencedColumnName = "email", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "message_type", nullable = false)
    private MessageType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isDeleted == null) isDeleted = false;
        if (type == null) type = MessageType.TEXT;
    }
}
