package com.example.lims.dto;

import java.time.Instant;
import java.util.UUID;

public class NotificationDto {

    private UUID id;
    private String userEmail;
    private String typeName;
    private String message;
    private Instant sentAt;

    public NotificationDto() {
    }

    public NotificationDto(UUID id, String userEmail, String typeName, String message, Instant sentAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.typeName = typeName;
        this.message = message;
        this.sentAt = sentAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
