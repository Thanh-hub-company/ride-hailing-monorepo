package com.example.telemetryservice.controller;

import com.example.telemetryservice.dto.GpsUpdatePayload;
import com.example.telemetryservice.dto.LocationUpdateDto;
import com.example.telemetryservice.service.LocationTrackingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;

@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private final LocationTrackingService locationTrackingService;
    private final ObjectMapper objectMapper;

    public TelemetryWebSocketHandler(LocationTrackingService locationTrackingService,
                                     ObjectMapper objectMapper) {
        this.locationTrackingService = locationTrackingService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        GpsUpdatePayload payload = objectMapper.readValue(message.getPayload(), GpsUpdatePayload.class);

        LocationUpdateDto updateDto = new LocationUpdateDto(
                payload.driverId(),
                payload.latitude(),
                payload.longitude(),
                payload.heading(),
                payload.speed(),
                Instant.now()
        );

        locationTrackingService.updateDriverLocation(updateDto);
        session.sendMessage(new TextMessage("{\"status\":\"SUCCESS\"}"));
    }
}