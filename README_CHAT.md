# 💬 Chat Write Service

Ứng dụng chat real-time sử dụng Spring Boot, WebSocket (STOMP), và PostgreSQL.

## 📋 Tổng quan

Chat Write Service là một ứng dụng backend cung cấp:
- **Real-time messaging** qua WebSocket (STOMP protocol)
- **REST API** để quản lý conversations
- **PostgreSQL** database để lưu trữ dữ liệu
- **Kafka integration** (optional) cho event streaming

## ✨ Tính năng

- ✅ **Gửi/nhận tin nhắn real-time** qua WebSocket
- ✅ **Quản lý conversation** (create, read, delete)
- ✅ **Quản lý participants** (add, remove)
- ✅ **Typing indicator** - hiển thị khi người dùng đang gõ
- ✅ **Message types** - Text, Image, File
- ✅ **Conversation types** - Private (1-1), Group
- ✅ **Message history** với pagination
- ✅ **Auto-create users** khi thêm vào conversation
- ✅ **Exception handling** với custom errors
- ✅ **Demo UI** để test nhanh

## 🏗️ Kiến trúc

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │
       ├─── WebSocket (/ws) ──────┐
       │                           │
       └─── REST API (/api) ───────┤
                                   │
                          ┌────────▼─────────┐
                          │   Spring Boot    │
                          │   Application    │
                          └────────┬─────────┘
                                   │
                          ┌────────▼─────────┐
                          │   PostgreSQL     │
                          │    Database      │
                          └──────────────────┘
```

### Technology Stack
- **Backend**: Spring Boot 3.5.9
- **Java**: 25
- **Database**: PostgreSQL
- **WebSocket**: STOMP over SockJS
- **Security**: Spring Security + OAuth2 (optional)
- **Message Broker**: Kafka (optional)
- **Build Tool**: Maven

## 📂 Cấu trúc Project

```
src/main/java/com/example/chatwriteservice/
├── config/              # Configuration classes
│   ├── KafkaConfig.java
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── WebSocketConfig.java
├── controller/          # REST & WebSocket controllers
│   ├── ConversationController.java
│   └── WebSocketMessageController.java
├── dto/                 # Data Transfer Objects
│   ├── ConversationRequest.java
│   ├── ConversationResponse.java
│   ├── MessageRequest.java
│   └── MessageResponse.java
├── entity/              # JPA Entities
│   ├── Conversation.java
│   ├── ConversationType.java
│   ├── Message.java
│   ├── MessageId.java
│   ├── MessageType.java
│   ├── Participant.java
│   └── User.java
├── exception/           # Custom exceptions
│   ├── BadRequestException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/          # JPA Repositories
│   ├── ConversationRepository.java
│   ├── MessageRepository.java
│   ├── ParticipantRepository.java
│   └── UserRepository.java
└── service/             # Business logic
    ├── ConversationService.java
    └── MessageService.java
```

## 🚀 Quick Start

### Prerequisites
- Java 25
- PostgreSQL 14+
- Maven 3.8+

### 1. Clone & Setup
```bash
git clone <repository-url>
cd chat-write-service
```

### 2. Cấu hình Database
Tạo file `.env`:
```properties
SQL_DB_HOST=localhost
SQL_DB_PORT=5432
POSTGRES_DB=chat_db
SQL_DB_USER=postgres
SQL_DB_PASS=your_password

KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_SCHEMA_REGISTRY_URL=http://localhost:8081
```

Tạo database:
```sql
CREATE DATABASE chat_db;
```

### 3. Chạy Application
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 4. Test
Mở trình duyệt: `http://localhost:8080/chat-demo.html`

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [QUICKSTART.md](QUICKSTART.md) | Hướng dẫn nhanh cho người mới |
| [CHAT_README.md](CHAT_README.md) | Chi tiết về chat features |
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | API endpoints reference |
| [FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md) | Tích hợp với frontend |
| [Chat-API.postman_collection.json](Chat-API.postman_collection.json) | Postman collection |

## 🔌 API Endpoints

### REST API
```
POST   /api/conversations              - Tạo conversation
GET    /api/conversations/{id}         - Lấy conversation
GET    /api/conversations/user/{email} - Lấy conversations của user
DELETE /api/conversations/{id}         - Xóa conversation
POST   /api/conversations/{id}/participants?email={email} - Thêm participant
DELETE /api/conversations/{id}/participants/{email}       - Xóa participant
GET    /api/conversations/{id}/messages?page=0&size=50    - Lấy messages
```

### WebSocket
```
Connect:    /ws
Send:       /app/chat.sendMessage
Subscribe:  /topic/conversation/{id}
```

## 📊 Database Schema

### Tables
- `users` - Người dùng
- `conversations` - Cuộc hội thoại
- `participants` - Người tham gia conversation
- `messages` - Tin nhắn

### Relationships
```
User 1---* Participant *---1 Conversation
User 1---* Message *---1 Conversation
```

## 🧪 Testing

### Run Tests
```bash
./mvnw test
```

### Manual Testing
1. **Demo UI**: http://localhost:8080/chat-demo.html
2. **Postman**: Import `Chat-API.postman_collection.json`
3. **cURL**: See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## 🔒 Security

⚠️ **Lưu ý**: Demo hiện tại **KHÔNG có authentication**. Tất cả endpoints đều public.

### Để enable authentication:
1. Uncomment các dòng trong `SecurityConfig.java`
2. Configure Keycloak hoặc OAuth2 provider
3. Thêm JWT token vào requests

```java
// SecurityConfig.java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
```

## 🔧 Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${SQL_DB_HOST}:${SQL_DB_PORT}/${POSTGRES_DB}
    username: ${SQL_DB_USER}
    password: ${SQL_DB_PASS}
```

### WebSocket Config
```java
// WebSocketConfig.java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns("*")
    .withSockJS();
```

## 🐛 Troubleshooting

### Database Connection Error
```
# Kiểm tra PostgreSQL đã chạy
# Kiểm tra credentials trong .env
# Kiểm tra database đã được tạo
```

### WebSocket Connection Failed
```
# Kiểm tra CORS settings
# Kiểm tra URL: http://localhost:8080/ws
# Kiểm tra browser console cho errors
```

### Messages Not Received
```
# Kiểm tra đã subscribe đúng conversation ID
# Kiểm tra user là participant của conversation
# Kiểm tra WebSocket connection status
```

## 📈 Performance Tips

1. **Pagination**: Limit message queries (max 100 per request)
2. **Connection Pooling**: Configure HikariCP
3. **Caching**: Add Redis for frequently accessed data
4. **Message Queue**: Use Kafka for high-volume messaging

## 🚧 Roadmap / TODO

- [ ] Add authentication with Keycloak
- [ ] Implement file upload for IMAGE/FILE messages
- [ ] Add message reactions (emoji)
- [ ] Add message editing/deletion
- [ ] Add read receipts
- [ ] Add push notifications
- [ ] Add rate limiting
- [ ] Add message search
- [ ] Add conversation archiving
- [ ] Add user presence (online/offline)

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

This project is for educational purposes.

## 👥 Authors

- Your Name

## 📞 Support

For issues and questions:
- GitHub Issues: [Create an issue]
- Documentation: See `/docs` folder
- Demo: http://localhost:8080/chat-demo.html

---

**Happy Coding! 🚀**
