// src/main/java/com/clickkart/auditlog/repository/AuditChainHeadRepository.java
package com.clickkart.auditlog.repository;

import com.clickkart.auditlog.entity.AuditChainHeadEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Deliberately extends the bare Spring Data {@code Repository} marker (not {@code JpaRepository})
 * - this singleton bookkeeping row has exactly two legitimate operations, save and lock-and-read.
 */
@Repository
public interface AuditChainHeadRepository extends org.springframework.data.repository.Repository<AuditChainHeadEntity, Long> {

    AuditChainHeadEntity save(AuditChainHeadEntity head);

    Optional<AuditChainHeadEntity> findById(Long id);

    /** Locks the singleton row for the duration of the caller's transaction, serializing every concurrent append onto this one row. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from AuditChainHeadEntity h where h.id = :id")
    Optional<AuditChainHeadEntity> lockForUpdate(@Param("id") Long id);
}
