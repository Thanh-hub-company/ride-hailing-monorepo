package com.example.telemetryservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Kênh client đăng ký lắng nghe dữ liệu từ server (/topic/...)
        registry.enableSimpleBroker("/topic");
        // Prefix dành cho client gửi dữ liệu lên server (/app/...)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm kết nối STOMP WebSocket cho Mobile App
        registry.addEndpoint("/ws/telemetry")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}