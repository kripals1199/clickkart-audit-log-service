// src/main/java/com/clickkart/auditlog/service/AuditTrailService.java
package com.clickkart.auditlog.service;

import com.clickkart.auditlog.dto.request.AuditEventRequest;
import com.clickkart.auditlog.entity.AuditLogEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * The platform's central, tamper-evident audit trail - the sink every other service's own
 * "record this write" call ultimately reaches. Same hash-chain-append pattern Auth Service
 * proved out internally, generalized here as the real, shared system of record.
 */
public interface AuditTrailService {

    /** Genesis hash for the very first entry in the chain - {@code AuditChainSeeder} creates the singleton head row with this as its starting {@code lastEntryHash}. */
    String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    /** Appends one entry to the hash chain under the chain-head lock - see {@code AuditChainHeadRepository.lockForUpdate}. */
    void record(AuditEventRequest request);

    /** Recomputes every entry's hash and verifies the chain links, in order - {@code intact:false} at the first mismatch, if any. */
    ChainIntegrityReport verifyChainIntegrity();

    Page<AuditLogEntryEntity> browse(Pageable pageable);
}
