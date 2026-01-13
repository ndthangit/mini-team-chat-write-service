# Chat Write Service - Architecture Diagrams

## 1. High-Level Architecture

```mermaid
graph TB
    Client[Client Application] --> API[REST API<br/>POST /v1/messages]
    API --> Controller[MessageController]
    Controller --> Service[MessageService]
    Service --> Validation{Validate<br/>Input}
    Validation -->|Invalid| Error1[Return 400]
    Validation -->|Valid| ParticipantCheck{Check<br/>Participant}
    ParticipantCheck -->|Not Found| Error2[Return 403]
    ParticipantCheck -->|Found| Transaction[Begin Transaction]
    Transaction --> SaveMessage[INSERT messages]
    SaveMessage --> SaveOutbox[INSERT outbox]
    SaveOutbox --> Commit[COMMIT]
    Commit --> Response[Return 201<br/>Created]
    
    Service --> MessageRepo[(MessageRepository)]
    Service --> OutboxRepo[(OutboxRepository)]
    Service --> ParticipantRepo[(ParticipantRepository)]
    
    MessageRepo --> DB[(PostgreSQL<br/>Database)]
    OutboxRepo --> DB
    ParticipantRepo --> DB
```

## 2. Transactional Outbox Pattern Flow

```mermaid
sequenceDiagram
    participant Client
    participant API as REST API
    participant Service as MessageService
    participant MsgRepo as MessageRepository
    participant OutboxRepo as OutboxRepository
    participant PartRepo as ParticipantRepository
    participant DB as PostgreSQL
    
    Client->>API: POST /v1/messages
    API->>Service: sendMessage(request)
    
    Service->>Service: validateMessageRequest()
    
    Service->>PartRepo: existsByConversationIdAndUserEmail()
    PartRepo->>DB: SELECT FROM participants
    DB-->>PartRepo: result
    PartRepo-->>Service: boolean
    
    alt Participant Not Found
        Service-->>API: throw ParticipantNotFoundException
        API-->>Client: 403 Forbidden
    else Participant Found
        Service->>DB: BEGIN TRANSACTION
        
        Service->>MsgRepo: save(message)
        MsgRepo->>DB: INSERT INTO messages
        DB-->>MsgRepo: saved message
        MsgRepo-->>Service: Message entity
        
        Service->>Service: createMessageEventPayload()
        
        Service->>OutboxRepo: save(outboxEvent)
        OutboxRepo->>DB: INSERT INTO outbox
        DB-->>OutboxRepo: saved outbox
        OutboxRepo-->>Service: Outbox entity
        
        Service->>DB: COMMIT TRANSACTION
        
        Service-->>API: MessageResponse
        API-->>Client: 201 Created
    end
```

## 3. Database Schema

```mermaid
erDiagram
    MESSAGES ||--o{ OUTBOX : "generates"
    PARTICIPANTS ||--o{ MESSAGES : "sends"
    
    MESSAGES {
        uuid id PK
        uuid conversation_id
        varchar sender_email
        varchar type
        text content
        timestamp created_at
    }
    
    OUTBOX {
        bigserial id PK
        varchar aggregate_type
        uuid aggregate_id FK
        varchar event_type
        jsonb payload
        timestamp created_at
        timestamp processed_at
    }
    
    PARTICIPANTS {
        bigserial id PK
        uuid conversation_id
        varchar user_email
        timestamp joined_at
    }
```

## 4. Component Diagram

```mermaid
graph LR
    subgraph "Presentation Layer"
        Controller[MessageController]
    end
    
    subgraph "Business Layer"
        Service[MessageService]
        Exception[GlobalExceptionHandler]
    end
    
    subgraph "Data Access Layer"
        MessageRepo[MessageRepository]
        OutboxRepo[OutboxRepository]
        PartRepo[ParticipantRepository]
    end
    
    subgraph "Domain Layer"
        Message[Message Entity]
        Outbox[Outbox Entity]
        Participant[Participant Entity]
        DTO[DTOs]
    end
    
    subgraph "Database"
        PostgreSQL[(PostgreSQL)]
    end
    
    Controller --> Service
    Controller --> Exception
    Service --> MessageRepo
    Service --> OutboxRepo
    Service --> PartRepo
    Service --> DTO
    MessageRepo --> Message
    OutboxRepo --> Outbox
    PartRepo --> Participant
    MessageRepo --> PostgreSQL
    OutboxRepo --> PostgreSQL
    PartRepo --> PostgreSQL
```

## 5. Data Flow - Successful Message Send

```mermaid
flowchart TD
    Start([Client Request]) --> Validate[Validate Input<br/>Email, Content, Type]
    Validate -->|Invalid| Error400[400 Bad Request]
    Validate -->|Valid| CheckPart[Check Participant<br/>in Database]
    CheckPart -->|Not Found| Error403[403 Forbidden]
    CheckPart -->|Found| BeginTx[BEGIN TRANSACTION]
    BeginTx --> InsertMsg[INSERT INTO messages<br/>Generate UUID<br/>Set timestamp]
    InsertMsg --> CreatePayload[Create JSON Payload<br/>with message data]
    CreatePayload --> InsertOutbox[INSERT INTO outbox<br/>aggregate_type: MESSAGE<br/>event_type: MESSAGE_CREATED]
    InsertOutbox --> Commit[COMMIT TRANSACTION]
    Commit --> Response[201 Created<br/>Return MessageResponse]
    Response --> End([End])
    
    style BeginTx fill:#90EE90
    style Commit fill:#90EE90
    style Error400 fill:#FFB6C1
    style Error403 fill:#FFB6C1
```

## 6. Event Processing (Future Implementation)

```mermaid
graph LR
    subgraph "Write Service"
        API[POST /v1/messages]
        Service[MessageService]
        DB1[(PostgreSQL<br/>messages + outbox)]
    end
    
    subgraph "Outbox Processor"
        Poller[Outbox Poller<br/>Scheduled Job]
        Publisher[Event Publisher]
    end
    
    subgraph "Event Bus"
        Kafka[Kafka Topic<br/>message-events]
    end
    
    subgraph "Consumers"
        ReadService[Read Service<br/>Update Read Model]
        NotificationService[Notification Service<br/>Push Notifications]
        SearchService[Search Service<br/>Index Messages]
    end
    
    API --> Service
    Service --> DB1
    DB1 --> Poller
    Poller --> Publisher
    Publisher --> Kafka
    Kafka --> ReadService
    Kafka --> NotificationService
    Kafka --> SearchService
```

## Key Design Decisions

### 1. Transactional Outbox Pattern
- **Problem**: Đảm bảo tính nhất quán giữa database write và event publish
- **Solution**: Lưu event vào cùng database transaction với message
- **Benefit**: Atomic operation, không mất event, eventual consistency

### 2. UUID for Message ID
- **Why**: 
  - Distributed system friendly
  - No central ID generator needed
  - Time-sortable (UUID v7) or random (UUID v4)

### 3. Email as User Identifier
- **Why**: 
  - Integration với CRM system
  - Human-readable
  - Unique identifier

### 4. Separate Participant Table
- **Why**:
  - Efficient permission check
  - Support multi-user conversations
  - Easy to query and index

### 5. JSONB for Event Payload
- **Why**:
  - Flexible schema
  - Native PostgreSQL support
  - Queryable with JSON operators
  - Easy to evolve event structure
