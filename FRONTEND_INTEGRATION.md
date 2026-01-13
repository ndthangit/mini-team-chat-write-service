# Frontend Integration Guide

Hướng dẫn tích hợp Chat Application với Frontend (React, Vue, Angular, etc.)

## 📦 Dependencies cần thiết

### NPM/Yarn
```bash
npm install sockjs-client @stomp/stompjs
# hoặc
yarn add sockjs-client @stomp/stompjs
```

### CDN (cho vanilla JS)
```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
```

---

## ⚛️ React Integration

### 1. Chat Service (hooks/useChatService.js)
```javascript
import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export const useChatService = (wsUrl, userEmail) => {
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const stompClientRef = useRef(null);
  const subscriptionsRef = useRef({});

  useEffect(() => {
    if (!userEmail) return;

    const socket = new SockJS(wsUrl);
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, 
      (frame) => {
        console.log('Connected:', frame);
        setConnected(true);
        stompClientRef.current = stompClient;

        // Subscribe to error queue
        stompClient.subscribe('/user/queue/errors', (message) => {
          console.error('Error:', message.body);
        });
      },
      (error) => {
        console.error('Connection error:', error);
        setConnected(false);
      }
    );

    return () => {
      if (stompClient.connected) {
        stompClient.disconnect();
      }
    };
  }, [wsUrl, userEmail]);

  const subscribeToConversation = (conversationId, callback) => {
    if (!stompClientRef.current?.connected) return;

    // Subscribe to messages
    const messageSub = stompClientRef.current.subscribe(
      `/topic/conversation/${conversationId}`,
      (message) => {
        const msg = JSON.parse(message.body);
        setMessages(prev => [...prev, msg]);
        if (callback) callback(msg);
      }
    );

    // Subscribe to typing indicators
    const typingSub = stompClientRef.current.subscribe(
      `/topic/conversation/${conversationId}/typing`,
      (message) => {
        const indicator = JSON.parse(message.body);
        // Handle typing indicator
      }
    );

    subscriptionsRef.current[conversationId] = { messageSub, typingSub };
  };

  const unsubscribeFromConversation = (conversationId) => {
    const subs = subscriptionsRef.current[conversationId];
    if (subs) {
      subs.messageSub.unsubscribe();
      subs.typingSub.unsubscribe();
      delete subscriptionsRef.current[conversationId];
    }
  };

  const sendMessage = (conversationId, content, type = 'TEXT') => {
    if (!stompClientRef.current?.connected) return;

    const message = {
      conversationId,
      senderEmail: userEmail,
      type,
      content
    };

    stompClientRef.current.send(
      '/app/chat.sendMessage',
      {},
      JSON.stringify(message)
    );
  };

  const sendTypingIndicator = (conversationId, isTyping) => {
    if (!stompClientRef.current?.connected) return;

    const indicator = {
      conversationId,
      userEmail,
      isTyping
    };

    stompClientRef.current.send(
      '/app/chat.typing',
      {},
      JSON.stringify(indicator)
    );
  };

  return {
    connected,
    messages,
    subscribeToConversation,
    unsubscribeFromConversation,
    sendMessage,
    sendTypingIndicator
  };
};
```

### 2. Chat Component (components/ChatRoom.jsx)
```jsx
import React, { useState, useEffect } from 'react';
import { useChatService } from '../hooks/useChatService';

const ChatRoom = ({ conversationId, userEmail }) => {
  const [messageText, setMessageText] = useState('');
  const { connected, messages, subscribeToConversation, sendMessage, sendTypingIndicator } = 
    useChatService('http://localhost:8080/ws', userEmail);

  useEffect(() => {
    if (connected && conversationId) {
      subscribeToConversation(conversationId, (msg) => {
        console.log('New message:', msg);
      });
    }
  }, [connected, conversationId]);

  const handleSend = () => {
    if (messageText.trim()) {
      sendMessage(conversationId, messageText);
      setMessageText('');
    }
  };

  const handleTyping = (e) => {
    setMessageText(e.target.value);
    sendTypingIndicator(conversationId, e.target.value.length > 0);
  };

  return (
    <div className="chat-room">
      <div className="messages">
        {messages.map((msg, idx) => (
          <div key={idx} className={msg.senderEmail === userEmail ? 'sent' : 'received'}>
            <strong>{msg.senderEmail}</strong>
            <p>{msg.content}</p>
            <small>{new Date(msg.createdAt).toLocaleTimeString()}</small>
          </div>
        ))}
      </div>
      <div className="input-area">
        <input
          type="text"
          value={messageText}
          onChange={handleTyping}
          onKeyPress={(e) => e.key === 'Enter' && handleSend()}
          placeholder="Type a message..."
        />
        <button onClick={handleSend} disabled={!connected}>
          Send
        </button>
      </div>
    </div>
  );
};

export default ChatRoom;
```

### 3. REST API Service (api/chatApi.js)
```javascript
const API_BASE = 'http://localhost:8080/api';

export const chatApi = {
  // Conversations
  async createConversation(data) {
    const response = await fetch(`${API_BASE}/conversations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return response.json();
  },

  async getConversationsByUser(email) {
    const response = await fetch(`${API_BASE}/conversations/user/${encodeURIComponent(email)}`);
    return response.json();
  },

  async getConversation(id) {
    const response = await fetch(`${API_BASE}/conversations/${id}`);
    return response.json();
  },

  async deleteConversation(id) {
    await fetch(`${API_BASE}/conversations/${id}`, {
      method: 'DELETE'
    });
  },

  async addParticipant(conversationId, email) {
    const response = await fetch(
      `${API_BASE}/conversations/${conversationId}/participants?email=${encodeURIComponent(email)}`,
      { method: 'POST' }
    );
    return response.json();
  },

  async removeParticipant(conversationId, email) {
    const response = await fetch(
      `${API_BASE}/conversations/${conversationId}/participants/${encodeURIComponent(email)}`,
      { method: 'DELETE' }
    );
    return response.json();
  },

  // Messages
  async getMessages(conversationId, page = 0, size = 50) {
    const response = await fetch(
      `${API_BASE}/conversations/${conversationId}/messages?page=${page}&size=${size}`
    );
    return response.json();
  }
};
```

---

## 🎨 Vue.js Integration

### 1. Composable (composables/useChatService.js)
```javascript
import { ref, onMounted, onUnmounted } from 'vue';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export function useChatService(wsUrl, userEmail) {
  const connected = ref(false);
  const messages = ref([]);
  let stompClient = null;
  const subscriptions = {};

  const connect = () => {
    const socket = new SockJS(wsUrl);
    stompClient = Stomp.over(socket);

    stompClient.connect({},
      (frame) => {
        console.log('Connected:', frame);
        connected.value = true;
      },
      (error) => {
        console.error('Connection error:', error);
        connected.value = false;
      }
    );
  };

  const subscribeToConversation = (conversationId) => {
    if (!stompClient?.connected) return;

    subscriptions[conversationId] = stompClient.subscribe(
      `/topic/conversation/${conversationId}`,
      (message) => {
        const msg = JSON.parse(message.body);
        messages.value.push(msg);
      }
    );
  };

  const sendMessage = (conversationId, content, type = 'TEXT') => {
    if (!stompClient?.connected) return;

    const message = {
      conversationId,
      senderEmail: userEmail.value,
      type,
      content
    };

    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
  };

  onMounted(() => {
    connect();
  });

  onUnmounted(() => {
    if (stompClient?.connected) {
      stompClient.disconnect();
    }
  });

  return {
    connected,
    messages,
    subscribeToConversation,
    sendMessage
  };
}
```

---

## 📐 Angular Integration

### 1. Chat Service (services/chat.service.ts)
```typescript
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import * as SockJS from 'sockjs-client';
import { Stomp, CompatClient } from '@stomp/stompjs';

interface Message {
  id: string;
  conversationId: string;
  senderEmail: string;
  type: string;
  content: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient: CompatClient | null = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  private messages$ = new BehaviorSubject<Message[]>([]);

  constructor() {}

  connect(wsUrl: string, userEmail: string): void {
    const socket = new SockJS(wsUrl);
    this.stompClient = Stomp.over(socket);

    this.stompClient.connect({},
      (frame) => {
        console.log('Connected:', frame);
        this.connected$.next(true);
      },
      (error) => {
        console.error('Connection error:', error);
        this.connected$.next(false);
      }
    );
  }

  subscribeToConversation(conversationId: string): void {
    if (!this.stompClient?.connected) return;

    this.stompClient.subscribe(
      `/topic/conversation/${conversationId}`,
      (message) => {
        const msg: Message = JSON.parse(message.body);
        const current = this.messages$.value;
        this.messages$.next([...current, msg]);
      }
    );
  }

  sendMessage(conversationId: string, senderEmail: string, content: string, type = 'TEXT'): void {
    if (!this.stompClient?.connected) return;

    const message = {
      conversationId,
      senderEmail,
      type,
      content
    };

    this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
  }

  getConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  getMessages(): Observable<Message[]> {
    return this.messages$.asObservable();
  }

  disconnect(): void {
    if (this.stompClient?.connected) {
      this.stompClient.disconnect();
    }
  }
}
```

---

## 🌐 Vanilla JavaScript (cho simple pages)

```javascript
class ChatClient {
  constructor(wsUrl, apiUrl, userEmail) {
    this.wsUrl = wsUrl;
    this.apiUrl = apiUrl;
    this.userEmail = userEmail;
    this.stompClient = null;
    this.connected = false;
  }

  connect() {
    return new Promise((resolve, reject) => {
      const socket = new SockJS(this.wsUrl);
      this.stompClient = Stomp.over(socket);

      this.stompClient.connect({},
        (frame) => {
          this.connected = true;
          resolve(frame);
        },
        (error) => {
          this.connected = false;
          reject(error);
        }
      );
    });
  }

  subscribeToConversation(conversationId, onMessage) {
    if (!this.connected) throw new Error('Not connected');

    return this.stompClient.subscribe(
      `/topic/conversation/${conversationId}`,
      (message) => {
        const msg = JSON.parse(message.body);
        onMessage(msg);
      }
    );
  }

  sendMessage(conversationId, content, type = 'TEXT') {
    if (!this.connected) throw new Error('Not connected');

    const message = {
      conversationId,
      senderEmail: this.userEmail,
      type,
      content
    };

    this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
  }

  async createConversation(title, type, participantEmails) {
    const response = await fetch(`${this.apiUrl}/conversations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, type, participantEmails })
    });
    return response.json();
  }

  async getMessages(conversationId, page = 0, size = 50) {
    const response = await fetch(
      `${this.apiUrl}/conversations/${conversationId}/messages?page=${page}&size=${size}`
    );
    return response.json();
  }
}

// Usage
const chat = new ChatClient(
  'http://localhost:8080/ws',
  'http://localhost:8080/api',
  'user@example.com'
);

await chat.connect();
chat.subscribeToConversation('conversation-id', (msg) => {
  console.log('New message:', msg);
});
chat.sendMessage('conversation-id', 'Hello!');
```

---

## 🔒 Thêm Authentication (JWT)

Nếu bạn enable authentication trong SecurityConfig:

```javascript
// React example
const token = localStorage.getItem('jwt_token');

// REST API calls
fetch(url, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

// WebSocket connection with auth
stompClient.connect(
  { 'Authorization': `Bearer ${token}` },
  onConnected,
  onError
);
```

---

## 📱 Mobile Integration (React Native)

```javascript
// Install dependencies
// npm install @stomp/stompjs react-native-url-polyfill

import { Stomp } from '@stomp/stompjs';
import 'react-native-url-polyfill/auto';

// Use same approach as React, but with fetch polyfill
```

---

## 🎯 Best Practices

1. **Connection Management**
   - Reconnect on connection loss
   - Handle connection state in UI
   - Clean up subscriptions on unmount

2. **Message Handling**
   - Deduplicate messages by ID
   - Handle message ordering
   - Implement pagination for history

3. **Error Handling**
   - Subscribe to error queue
   - Show user-friendly error messages
   - Log errors for debugging

4. **Performance**
   - Unsubscribe when leaving conversation
   - Limit message list size (virtual scrolling)
   - Debounce typing indicators

5. **Security**
   - Always use HTTPS/WSS in production
   - Validate user permissions server-side
   - Sanitize message content (XSS prevention)
