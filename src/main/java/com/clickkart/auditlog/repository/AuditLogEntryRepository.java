// src/main/java/com/clickkart/auditlog/repository/AuditLogEntryRepository.java
package com.clickkart.auditlog.repository;

import com.clickkart.auditlog.entity.AuditLogEntryEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Deliberately extends the bare Spring Data {@code Repository} marker, not {@code JpaRepository}
 * - an audit trail is append-only by design; this interface exposes no {@code deleteById}/{@code
 * deleteAll} at all.
 */
@Repository
public interface AuditLogEntryRepository extends org.springframework.data.repository.Repository<AuditLogEntryEntity, Long> {

    AuditLogEntryEntity save(AuditLogEntryEntity entry);

    long count();

    Page<AuditLogEntryEntity> findAllByOrderByIdAsc(Pageable pageable);

    /**
     * Full-table read in chain order, for integrity verification. Loads the entire table into
     * memory - fine for an on-demand admin check at current scale; a checkpoint-based
     * incremental verification is the natural next step once the table is large.
     */
    List<AuditLogEntryEntity> findAllByOrderByIdAsc();
}
