# API Documentation

## 🌐 Base URL
```
http://localhost:8080
```

---

## 🔌 WebSocket Endpoints

### Connect to WebSocket
```
Endpoint: /ws
Protocol: SockJS + STOMP
```

### Subscribe Destinations

#### Nhận tin nhắn từ conversation
```
Destination: /topic/conversation/{conversationId}
Type: Subscribe
Message Format: MessageResponse (JSON)
```

#### Nhận typing indicator
```
Destination: /topic/conversation/{conversationId}/typing
Type: Subscribe
Message Format: TypingIndicator (JSON)
```

#### Nhận lỗi
```
Destination: /user/queue/errors
Type: Subscribe
Message Format: String
```

### Send Destinations

#### Gửi tin nhắn
```
Destination: /app/chat.sendMessage
Type: Send
Message Format: MessageRequest (JSON)
```

#### Gửi typing indicator
```
Destination: /app/chat.typing
Type: Send
Message Format: TypingIndicator (JSON)
```

---

## 🔗 REST API Endpoints

### Conversations

#### 1. Create Conversation
```http
POST /api/conversations
Content-Type: application/json

Request Body:
{
  "title": "string (optional)",
  "type": "PRIVATE | GROUP",
  "metadata": "string (optional)",
  "participantEmails": ["email1", "email2", ...]
}

Response: 201 Created
{
  "id": "uuid",
  "title": "string",
  "type": "PRIVATE | GROUP",
  "metadata": "string",
  "createdAt": "2026-01-13T10:00:00",
  "updatedAt": "2026-01-13T10:00:00",
  "participantEmails": ["email1", "email2"]
}

Errors:
- 400: Validation failed
- 500: Server error
```

#### 2. Get Conversation by ID
```http
GET /api/conversations/{id}

Response: 200 OK
{
  "id": "uuid",
  "title": "string",
  "type": "PRIVATE | GROUP",
  "metadata": "string",
  "createdAt": "2026-01-13T10:00:00",
  "updatedAt": "2026-01-13T10:00:00",
  "participantEmails": ["email1", "email2"]
}

Errors:
- 404: Conversation not found
```

#### 3. Get User's Conversations
```http
GET /api/conversations/user/{email}

Response: 200 OK
[
  {
    "id": "uuid",
    "title": "string",
    "type": "PRIVATE | GROUP",
    "metadata": "string",
    "createdAt": "2026-01-13T10:00:00",
    "updatedAt": "2026-01-13T10:00:00",
    "participantEmails": ["email1", "email2"]
  },
  ...
]
```

#### 4. Delete Conversation
```http
DELETE /api/conversations/{id}

Response: 204 No Content

Errors:
- 404: Conversation not found
```

#### 5. Add Participant
```http
POST /api/conversations/{id}/participants?email={email}

Response: 200 OK
{
  "id": "uuid",
  "title": "string",
  "type": "PRIVATE | GROUP",
  "metadata": "string",
  "createdAt": "2026-01-13T10:00:00",
  "updatedAt": "2026-01-13T10:00:00",
  "participantEmails": ["email1", "email2", "new_email"]
}

Errors:
- 400: User is already a participant
- 404: Conversation not found
```

#### 6. Remove Participant
```http
DELETE /api/conversations/{id}/participants/{email}

Response: 200 OK
{
  "id": "uuid",
  "title": "string",
  "type": "PRIVATE | GROUP",
  "metadata": "string",
  "createdAt": "2026-01-13T10:00:00",
  "updatedAt": "2026-01-13T10:00:00",
  "participantEmails": ["email1"]
}

Errors:
- 400: User is not a participant
- 404: Conversation not found
```

### Messages

#### 7. Get Conversation Messages (Paginated)
```http
GET /api/conversations/{id}/messages?page={page}&size={size}

Parameters:
- page: int (default: 0)
- size: int (default: 50)

Response: 200 OK
{
  "content": [
    {
      "id": "uuid",
      "conversationId": "uuid",
      "senderEmail": "string",
      "type": "TEXT | IMAGE | FILE",
      "content": "string",
      "createdAt": "2026-01-13T10:00:00",
      "isDeleted": false
    },
    ...
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "sort": {...},
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 10,
  "totalElements": 500,
  "last": false,
  "size": 50,
  "number": 0,
  "sort": {...},
  "numberOfElements": 50,
  "first": true,
  "empty": false
}

Errors:
- 404: Conversation not found
```

---

## 📦 Data Models

### MessageRequest
```json
{
  "conversationId": "uuid (required)",
  "senderEmail": "string (required)",
  "type": "TEXT | IMAGE | FILE (required)",
  "content": "string (optional)"
}
```

### MessageResponse
```json
{
  "id": "uuid",
  "conversationId": "uuid",
  "senderEmail": "string",
  "type": "TEXT | IMAGE | FILE",
  "content": "string",
  "createdAt": "ISO-8601 datetime",
  "isDeleted": "boolean"
}
```

### ConversationRequest
```json
{
  "title": "string (optional)",
  "type": "PRIVATE | GROUP (required)",
  "metadata": "string (optional)",
  "participantEmails": ["string", "string", ...] (required, min 1)
}
```

### ConversationResponse
```json
{
  "id": "uuid",
  "title": "string",
  "type": "PRIVATE | GROUP",
  "metadata": "string",
  "createdAt": "ISO-8601 datetime",
  "updatedAt": "ISO-8601 datetime",
  "participantEmails": ["string", "string", ...]
}
```

### TypingIndicator
```json
{
  "conversationId": "string (uuid)",
  "userEmail": "string",
  "isTyping": "boolean"
}
```

---

## 🔍 Query Parameters

### Messages Pagination
- `page`: Trang hiện tại (0-indexed, default: 0)
- `size`: Số lượng tin nhắn mỗi trang (default: 50, max: 100)

Example:
```
/api/conversations/{id}/messages?page=2&size=20
```

---

## 📊 Response Codes

### Success Codes
- `200 OK`: Request thành công
- `201 Created`: Resource được tạo thành công
- `204 No Content`: Request thành công, không có dữ liệu trả về

### Error Codes
- `400 Bad Request`: Dữ liệu request không hợp lệ
- `404 Not Found`: Resource không tồn tại
- `500 Internal Server Error`: Lỗi server

---

## 🔐 Authentication (Future Implementation)

Khi enable authentication, thêm header:
```http
Authorization: Bearer {jwt_token}
```

---

## 📝 Examples

### Example 1: Tạo conversation và gửi tin nhắn

#### Step 1: Tạo conversation
```bash
curl -X POST http://localhost:8080/api/conversations \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Project Discussion",
    "type": "GROUP",
    "participantEmails": ["alice@example.com", "bob@example.com"]
  }'
```

Response:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Project Discussion",
  "type": "GROUP",
  ...
}
```

#### Step 2: Kết nối WebSocket
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);
stompClient.connect({}, function(frame) {
  console.log('Connected');
});
```

#### Step 3: Subscribe to conversation
```javascript
stompClient.subscribe('/topic/conversation/123e4567-e89b-12d3-a456-426614174000', 
  function(message) {
    console.log('Received:', JSON.parse(message.body));
  }
);
```

#### Step 4: Gửi tin nhắn
```javascript
const msg = {
  conversationId: "123e4567-e89b-12d3-a456-426614174000",
  senderEmail: "alice@example.com",
  type: "TEXT",
  content: "Hello team!"
};
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(msg));
```

### Example 2: Load lịch sử tin nhắn
```bash
curl "http://localhost:8080/api/conversations/123e4567-e89b-12d3-a456-426614174000/messages?page=0&size=50"
```

### Example 3: Thêm participant
```bash
curl -X POST "http://localhost:8080/api/conversations/123e4567-e89b-12d3-a456-426614174000/participants?email=charlie@example.com"
```

---

## 🧪 Testing Tools

### Postman Collection
Import file: `Chat-API.postman_collection.json`

### Browser Demo
URL: `http://localhost:8080/chat-demo.html`

### cURL Examples
See examples above

---

## 🐛 Error Responses

### Validation Error (400)
```json
{
  "timestamp": "2026-01-13T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "participantEmails": "Participant emails are required",
    "type": "Conversation type is required"
  },
  "path": "/api/conversations"
}
```

### Not Found Error (404)
```json
{
  "timestamp": "2026-01-13T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Conversation not found",
  "path": "/api/conversations/invalid-uuid"
}
```

### Server Error (500)
```json
{
  "timestamp": "2026-01-13T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Database connection failed",
  "path": "/api/conversations"
}
```

---

## 📌 Notes

1. **UUID Format**: Tất cả IDs sử dụng UUID format (RFC 4122)
2. **Datetime Format**: ISO-8601 format với timezone UTC+7 (Asia/Ho_Chi_Minh)
3. **Email Format**: Phải là email hợp lệ
4. **Message Types**: TEXT, IMAGE, FILE
5. **Conversation Types**: PRIVATE (1-1), GROUP (nhiều người)
6. **Auto User Creation**: User sẽ được tự động tạo nếu chưa tồn tại khi thêm vào conversation

---

## 🔄 Rate Limiting (TODO)

Chưa implement, nên cẩn thận với:
- Số lượng tin nhắn gửi mỗi phút
- Số lượng connection mỗi user
- Pagination size (max 100)
