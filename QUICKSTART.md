# Quick Start Guide - Chat Application

## 🚀 Khởi động nhanh

### Bước 1: Chuẩn bị Database
Đảm bảo PostgreSQL đã chạy và tạo database:
```sql
CREATE DATABASE chat_db;
```

### Bước 2: Cấu hình môi trường
Tạo file `.env` trong thư mục gốc project:
```properties
SQL_DB_HOST=localhost
SQL_DB_PORT=5432
POSTGRES_DB=chat_db
SQL_DB_USER=postgres
SQL_DB_PASS=your_password

KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_SCHEMA_REGISTRY_URL=http://localhost:8081
```

### Bước 3: Chạy ứng dụng
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Bước 4: Test với Demo UI
Mở trình duyệt và truy cập:
```
http://localhost:8080/chat-demo.html
```

---

## 📝 Hướng dẫn sử dụng Demo UI

### 1. Kết nối WebSocket
- **WebSocket URL**: `http://localhost:8080/ws` (mặc định)
- **Email**: Nhập email của bạn (ví dụ: `alice@example.com`)
- Click **Connect**
- Trạng thái chuyển sang "Connected as alice@example.com"

### 2. Tạo Conversation
- **Title**: Nhập tiêu đề (có thể bỏ trống)
- **Type**: Chọn PRIVATE hoặc GROUP
- **Participants**: Nhập danh sách email, phân cách bằng dấu phẩy
  ```
  alice@example.com, bob@example.com, charlie@example.com
  ```
- Click **Create Conversation**
- Lưu lại ID của conversation được tạo

### 3. Tham gia Conversation
- Click **Load My Conversations**
- Chọn conversation từ dropdown
- Click **Subscribe to Selected Conversation**
- Tin nhắn cũ sẽ được load tự động

### 4. Gửi tin nhắn
- Nhập tin nhắn vào text area
- Chọn loại tin nhắn (TEXT, IMAGE, FILE)
- Click **Send Message** hoặc nhấn Enter
- Tin nhắn sẽ xuất hiện real-time

### 5. Test với nhiều người dùng
- Mở tab mới trong trình duyệt
- Kết nối với email khác (ví dụ: `bob@example.com`)
- Subscribe vào cùng conversation
- Gửi tin nhắn và xem real-time update

---

## 🔧 Test với cURL

### Tạo conversation
```bash
curl -X POST http://localhost:8080/api/conversations \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Chat Room",
    "type": "GROUP",
    "participantEmails": ["alice@example.com", "bob@example.com"]
  }'
```

### Lấy conversations của user
```bash
curl http://localhost:8080/api/conversations/user/alice@example.com
```

### Lấy tin nhắn của conversation
```bash
curl "http://localhost:8080/api/conversations/{conversationId}/messages?page=0&size=50"
```

---

## 📱 Test với JavaScript (Browser Console)

```javascript
// Kết nối WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected');
    
    // Subscribe để nhận tin nhắn
    stompClient.subscribe('/topic/conversation/YOUR_CONVERSATION_ID', function(message) {
        console.log('Message received:', JSON.parse(message.body));
    });
    
    // Gửi tin nhắn
    const msg = {
        conversationId: "YOUR_CONVERSATION_ID",
        senderEmail: "alice@example.com",
        type: "TEXT",
        content: "Hello from console!"
    };
    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(msg));
});
```

---

## 🎯 Các Scenarios Test

### Scenario 1: Chat 1-1
1. Tạo PRIVATE conversation với 2 người
2. Mở 2 tab trình duyệt với 2 email khác nhau
3. Subscribe vào conversation
4. Gửi tin nhắn qua lại

### Scenario 2: Group Chat
1. Tạo GROUP conversation với 3+ người
2. Mở nhiều tab với các email khác nhau
3. Tất cả subscribe vào conversation
4. Gửi tin nhắn và xem tất cả nhận được

### Scenario 3: Thêm/xóa participant
1. Tạo conversation với 2 người
2. Dùng API hoặc UI để thêm người thứ 3
3. Người mới subscribe và tham gia chat
4. Xóa 1 người và kiểm tra họ không nhận được tin nhắn mới

### Scenario 4: Typing Indicator
1. Subscribe vào conversation
2. Gõ tin nhắn (không gửi)
3. Tab khác sẽ thấy "user is typing..."

---

## 🐛 Troubleshooting

### Lỗi kết nối WebSocket
- Kiểm tra ứng dụng đã chạy chưa
- Kiểm tra URL đúng: `http://localhost:8080/ws`
- Kiểm tra CORS settings trong WebConfig.java

### Không nhận được tin nhắn
- Kiểm tra đã subscribe đúng conversation ID chưa
- Kiểm tra console để xem lỗi
- Kiểm tra user có trong danh sách participants không

### Database errors
- Kiểm tra PostgreSQL đã chạy
- Kiểm tra connection string trong application.yml
- Kiểm tra các bảng đã được tạo (JPA auto-create)

---

## 📚 Tài liệu bổ sung
- [CHAT_README.md](CHAT_README.md) - Tài liệu chi tiết
- [Chat-API.postman_collection.json](Chat-API.postman_collection.json) - Postman collection
- [chat-demo.html](src/main/resources/static/chat-demo.html) - Source code demo UI

---

## ⚠️ Lưu ý
- Demo này không có authentication - tất cả endpoint đều public
- Trong production cần thêm JWT/OAuth2
- Cần validate permissions trước khi gửi tin nhắn
- Rate limiting cho WebSocket connections
