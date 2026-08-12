// src/main/java/com/clickkart/auditlog/entity/AuditChainHeadEntity.java
package com.clickkart.auditlog.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Exactly one row, always id {@link #SINGLETON_ID} (see {@link AssignedOrSequenceIdGenerator}).
 * Locked (see {@code AuditChainHeadRepository.lockForUpdate}) for the duration of every append,
 * which is what keeps the hash chain a single unbroken sequence rather than letting concurrent
 * writers fork it.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "audit_chain_head")
public class AuditChainHeadEntity extends BaseEntity {

    public static final long SINGLETON_ID = 1L;

    @Column(name = "last_entry_hash", nullable = false, length = 64)
    private String lastEntryHash;

    @Column(name = "entry_count", nullable = false)
    private long entryCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AuditChainHeadEntity(String genesisHash) {
        this.lastEntryHash = genesisHash;
        this.entryCount = 0;
        this.updatedAt = Instant.now();
    }

    public void advance(String newEntryHash) {
        this.lastEntryHash = newEntryHash;
        this.entryCount++;
        this.updatedAt = Instant.now();
    }
}
