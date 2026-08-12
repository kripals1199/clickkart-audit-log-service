// src/main/java/com/clickkart/auditlog/entity/AuditLogEntryEntity.java
package com.clickkart.auditlog.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only, hash-chained audit entry - the platform's central system of record, replicating
 * Auth Service's own proven internal pattern (same hashing/chaining approach, already reviewed
 * and fixed this session) rather than inventing a new one. {@code action} is plain {@code
 * String}, not an enum - this service is a decoupled sink for every future caller's own action
 * vocabulary (Rule 4: no shared library, so no shared enum either).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "audit_log_entries",
        indexes = {
            @Index(name = "idx_audit_log_entries_actor", columnList = "actor"),
            @Index(name = "idx_audit_log_entries_correlation_id", columnList = "correlation_id"),
            @Index(name = "idx_audit_log_entries_occurred_at", columnList = "occurred_at")
        })
public class AuditLogEntryEntity extends BaseEntity {

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "correlation_id", nullable = false, updatable = false, length = 36)
    private String correlationId;

    /** The reporting service's actor identifier (e.g. Auth Service's {@code ClickKartUserEntity.publicId}, or "system"). */
    @Column(name = "actor", nullable = false, updatable = false, length = 64)
    private String actor;

    @Column(name = "action", nullable = false, updatable = false, length = 60)
    private String action;

    @Column(name = "ip_address", nullable = false, updatable = false, length = 45)
    private String ipAddress;

    @Column(name = "details", updatable = false, length = 2000)
    private String details;

    @Column(name = "previous_entry_hash", nullable = false, updatable = false, length = 64)
    private String previousEntryHash;

    @Column(name = "entry_hash", nullable = false, updatable = false, length = 64)
    private String entryHash;

    private AuditLogEntryEntity(
            Instant occurredAt,
            String correlationId,
            String actor,
            String action,
            String ipAddress,
            String details,
            String previousEntryHash,
            String entryHash) {
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
        this.actor = actor;
        this.action = action;
        this.ipAddress = ipAddress;
        this.details = details;
        this.previousEntryHash = previousEntryHash;
        this.entryHash = entryHash;
    }

    /**
     * Builds a new entry linked to {@code previousEntryHash}, computing this entry's own hash
     * over its canonical field representation. The caller ({@code AuditTrailService}) is
     * responsible for supplying the correct, current chain-head hash under a lock - this factory
     * only computes the digest, it does not read or update any shared state itself.
     */
    public static AuditLogEntryEntity create(
            Instant occurredAt, String correlationId, String actor, String action, String ipAddress, String details, String previousEntryHash) {
        String payload = canonicalPayload(occurredAt, correlationId, actor, action, ipAddress, details, previousEntryHash);
        String entryHash = sha256Hex(payload);
        return new AuditLogEntryEntity(occurredAt, correlationId, actor, action, ipAddress, details, previousEntryHash, entryHash);
    }

    /**
     * Recomputes what this row's {@code entryHash} should be, from its own persisted fields -
     * used by integrity verification to detect tampering. Field order/separators here must never
     * change without a documented migration plan for the whole chain.
     */
    public String recomputeHash() {
        String payload = canonicalPayload(occurredAt, correlationId, actor, action, ipAddress, details, previousEntryHash);
        return sha256Hex(payload);
    }

    private static String canonicalPayload(
            Instant occurredAt, String correlationId, String actor, String action, String ipAddress, String details, String previousEntryHash) {
        return String.join(
                "|",
                previousEntryHash,
                occurredAt.toString(),
                correlationId,
                actor,
                action,
                ipAddress,
                details == null ? "" : details);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
