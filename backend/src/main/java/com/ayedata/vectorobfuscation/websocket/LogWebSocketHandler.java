package com.ayedata.vectorobfuscation.websocket;

import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LogWebSocketHandler.class);

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    // Standard Java Constructor
    public LogWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("New UI client connected to log stream. Session ID: {}", session.getId());
        broadcastLog("SYSTEM", 0, "Connected to Secure Vector Obfuscation Stream.");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("UI client disconnected. Session ID: {}", session.getId());
    }

    public void broadcastLog(String operation, long latencyMs, String message) {
        try {
            Map<String, Object> logEvent = Map.of(
                    "timestamp", Instant.now().toString(),
                    "operation", operation,
                    "latencyMs", latencyMs,
                    "message", message);

            String jsonPayload = objectMapper.writeValueAsString(logEvent);
            TextMessage textMessage = new TextMessage(jsonPayload);

            for (WebSocketSession session : sessions) {
                if (session.isOpen())
                    session.sendMessage(textMessage);
            }
        } catch (IOException e) {
            log.error("Failed to broadcast log message", e);
        }
    }
}