# Chat Application với WebSocket

Ứng dụng chat real-time sử dụng WebSocket (STOMP) cho việc gửi tin nhắn và HTTP REST API cho quản lý conversation.

## Kiến trúc

- **WebSocket (STOMP)**: Dùng cho việc gửi/nhận tin nhắn real-time
- **HTTP REST API**: Dùng cho việc tạo, quản lý conversation và load lịch sử tin nhắn
- **Database**: PostgreSQL với JPA/Hibernate

## Các thành phần đã tạo

### 1. Configuration
- `WebSocketConfig.java`: Cấu hình WebSocket với STOMP messaging
  - Endpoint: `/ws`
  - Application prefix: `/app`
  - Broker: `/topic`, `/queue`

### 2. DTOs
- `MessageRequest.java`: Request gửi tin nhắn
- `MessageResponse.java`: Response tin nhắn
- `ConversationRequest.java`: Request tạo conversation
- `ConversationResponse.java`: Response conversation

### 3. Repositories
- `MessageRepository.java`: Thao tác với messages table
- `ConversationRepository.java`: Thao tác với conversations table
- `ParticipantRepository.java`: Thao tác với participants table
- `UserRepository.java`: Thao tác với users table

### 4. Services
- `MessageService.java`: Business logic cho tin nhắn
- `ConversationService.java`: Business logic cho conversation

### 5. Controllers
- `WebSocketMessageController.java`: Handle WebSocket messages
- `ConversationController.java`: REST API cho conversation

## REST API Endpoints

### Conversations

#### Tạo conversation mới
```http
POST /api/conversations
Content-Type: application/json

{
  "title": "My Chat Room",
  "type": "PRIVATE",  // hoặc "GROUP"
  "metadata": null,
  "participantEmails": [
    "user1@example.com",
    "user2@example.com"
  ]
}
```

#### Lấy conversation theo ID
```http
GET /api/conversations/{conversationId}
```

#### Lấy tất cả conversation của user
```http
GET /api/conversations/user/{email}
```

#### Xóa conversation
```http
DELETE /api/conversations/{conversationId}
```

#### Thêm participant
```http
POST /api/conversations/{conversationId}/participants?email=newuser@example.com
```

#### Xóa participant
```http
DELETE /api/conversations/{conversationId}/participants/{email}
```

#### Lấy tin nhắn của conversation (có phân trang)
```http
GET /api/conversations/{conversationId}/messages?page=0&size=50
```

## WebSocket

### Kết nối
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
});
```

### Subscribe để nhận tin nhắn
```javascript
// Subscribe to conversation messages
stompClient.subscribe('/topic/conversation/{conversationId}', function(message) {
    const msg = JSON.parse(message.body);
    console.log('Received message:', msg);
});
```

### Gửi tin nhắn
```javascript
const message = {
    conversationId: "uuid-here",
    senderEmail: "user@example.com",
    type: "TEXT",  // TEXT, IMAGE, FILE
    content: "Hello World!"
};

stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
```

### Typing indicator
```javascript
// Subscribe to typing indicators
stompClient.subscribe('/topic/conversation/{conversationId}/typing', function(message) {
    const indicator = JSON.parse(message.body);
    console.log('Typing:', indicator);
});

// Send typing indicator
const indicator = {
    conversationId: "uuid-here",
    userEmail: "user@example.com",
    isTyping: true
};

stompClient.send('/app/chat.typing', {}, JSON.stringify(indicator));
```

## Cách sử dụng

### 1. Cấu hình database trong application.yml
Đảm bảo các biến môi trường được set đúng:
- `SQL_DB_HOST`
- `SQL_DB_PORT`
- `POSTGRES_DB`
- `SQL_DB_USER`
- `SQL_DB_PASS`

### 2. Chạy ứng dụng
```bash
./mvnw spring-boot:run
```

### 3. Test với Demo UI
Mở trình duyệt và truy cập:
```
http://localhost:8080/chat-demo.html
```

### 4. Quy trình sử dụng

1. **Kết nối WebSocket**:
   - Nhập email của bạn
   - Click "Connect"

2. **Tạo Conversation**:
   - Nhập title (optional)
   - Chọn type (PRIVATE/GROUP)
   - Nhập danh sách email người tham gia (phân cách bằng dấu phẩy)
   - Click "Create Conversation"

3. **Load và Subscribe Conversation**:
   - Click "Load My Conversations"
   - Chọn conversation từ dropdown
   - Click "Subscribe to Selected Conversation"

4. **Chat**:
   - Nhập tin nhắn
   - Chọn message type
   - Click "Send Message" hoặc nhấn Enter

### 5. Test với nhiều client
Mở nhiều tab/cửa sổ trình duyệt với email khác nhau để test real-time messaging.

## Message Types
- `TEXT`: Tin nhắn văn bản
- `IMAGE`: Tin nhắn hình ảnh (lưu URL hoặc base64)
- `FILE`: Tin nhắn file (lưu URL hoặc metadata)

## Conversation Types
- `PRIVATE`: Chat 1-1
- `GROUP`: Chat nhóm

## Features
✅ Gửi/nhận tin nhắn real-time qua WebSocket
✅ Tạo và quản lý conversation qua REST API
✅ Typing indicator
✅ Load lịch sử tin nhắn với phân trang
✅ Quản lý participants
✅ Support nhiều loại tin nhắn (TEXT, IMAGE, FILE)
✅ Auto-create user nếu chưa tồn tại

## Error Handling
- Tin nhắn lỗi được gửi đến `/user/queue/errors`
- Các lỗi validation được trả về trong response
- Exception được log trong server

## Security Note
⚠️ Demo này chưa có authentication/authorization. Trong production, bạn cần:
- Thêm JWT hoặc OAuth2 authentication
- Validate user permissions trước khi gửi tin nhắn
- Secure WebSocket connections
- Add rate limiting

## Next Steps
- [ ] Thêm authentication với Keycloak (đã có config sẵn)
- [ ] Thêm file upload cho IMAGE/FILE messages
- [ ] Thêm message reactions
- [ ] Thêm message editing/deletion
- [ ] Thêm read receipts
- [ ] Thêm push notifications
