package com.example.lims.dto;

import java.time.Instant;
import java.util.UUID;

public class AuditDto {

    private UUID id;
    private String actionName;
    private String entityType;
    private UUID entityId;
    private String userEmail;
    private Instant changedAt;

    public AuditDto() {
    }

    public AuditDto(UUID id, String actionName, String entityType, UUID entityId,
                    String userEmail, Instant changedAt) {
        this.id = id;
        this.actionName = actionName;
        this.entityType = entityType;
        this.entityId = entityId;
        this.userEmail = userEmail;
        this.changedAt = changedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}
