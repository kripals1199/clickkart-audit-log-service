// src/main/java/com/clickkart/auditlog/dto/request/AuditEventRequest.java
package com.clickkart.auditlog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Matches Auth Service's {@code AuditEventRequest} shape exactly - verified field-for-field
 * against {@code clickkart-auth-service/.../feign/AuditEventRequest.java}: correlationId, actor,
 * action, ipAddress, timestamp, details. No {@code outcome} field travels over this wire (Auth
 * Service tracks success/failure only in its own internal audit trail, not in what it reports
 * here) - this service's entity mirrors that, it does not invent a field the sender never sends.
 * {@code action} is deliberately plain {@code String}, not tied to Auth Service's own {@code
 * AuditAction} enum (this service is a decoupled central sink for every future service's own
 * action vocabulary).
 */
public record AuditEventRequest(
        @NotBlank String correlationId,
        @NotBlank String actor,
        @NotBlank String action,
        @NotBlank String ipAddress,
        @NotNull Instant timestamp,
        String details) {}
