package com.example.gateway.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketGatewayHandler implements WebSocketHandler {

    // Map session ID -> User ID / Session Context
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        sessions.put(session.getId(), session);
        return session.receive()
                .doFinally(signalType -> sessions.remove(session.getId()))
                .then();
    }

    public void sendMessageToUser(String userId, String payload) {
        // Logic tìm session của user và gửi message
    }
}
