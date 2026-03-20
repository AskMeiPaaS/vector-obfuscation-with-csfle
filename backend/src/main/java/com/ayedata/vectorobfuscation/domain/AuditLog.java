package com.ayedata.vectorobfuscation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;
    private String eventType;
    private String method;
    private long latencyMs;
    private String message;
    private Instant timestamp;
    private Double relevancyScore;

    public AuditLog() {}

    public AuditLog(String id, String eventType, String method, long latencyMs, String message, Instant timestamp, Double relevancyScore) {
        this.id = id;
        this.eventType = eventType;
        this.method = method;
        this.latencyMs = latencyMs;
        this.message = message;
        this.timestamp = timestamp;
        this.relevancyScore = relevancyScore;
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Double getRelevancyScore() { return relevancyScore; }
    public void setRelevancyScore(Double relevancyScore) { this.relevancyScore = relevancyScore; }

    public static class AuditLogBuilder {
        private String id;
        private String eventType;
        private String method;
        private long latencyMs;
        private String message;
        private Instant timestamp;
        private Double relevancyScore;

        public AuditLogBuilder id(String id) { this.id = id; return this; }
        public AuditLogBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public AuditLogBuilder method(String method) { this.method = method; return this; }
        public AuditLogBuilder latencyMs(long latencyMs) { this.latencyMs = latencyMs; return this; }
        public AuditLogBuilder message(String message) { this.message = message; return this; }
        public AuditLogBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public AuditLogBuilder relevancyScore(Double relevancyScore) { this.relevancyScore = relevancyScore; return this; }

        public AuditLog build() {
            return new AuditLog(id, eventType, method, latencyMs, message, timestamp, relevancyScore);
        }
    }
}
