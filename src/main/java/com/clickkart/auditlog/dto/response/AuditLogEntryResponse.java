// src/main/java/com/clickkart/auditlog/dto/response/AuditLogEntryResponse.java
package com.clickkart.auditlog.dto.response;

import com.clickkart.auditlog.entity.AuditLogEntryEntity;
import java.time.Instant;

public record AuditLogEntryResponse(
        Long id,
        Instant occurredAt,
        String correlationId,
        String actor,
        String action,
        String ipAddress,
        String details,
        String previousEntryHash,
        String entryHash) {

    public static AuditLogEntryResponse from(AuditLogEntryEntity entity) {
        return new AuditLogEntryResponse(
                entity.getId(),
                entity.getOccurredAt(),
                entity.getCorrelationId(),
                entity.getActor(),
                entity.getAction(),
                entity.getIpAddress(),
                entity.getDetails(),
                entity.getPreviousEntryHash(),
                entity.getEntryHash());
    }
}
