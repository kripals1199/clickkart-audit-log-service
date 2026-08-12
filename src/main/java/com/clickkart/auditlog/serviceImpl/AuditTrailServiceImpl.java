// src/main/java/com/clickkart/auditlog/serviceImpl/AuditTrailServiceImpl.java
package com.clickkart.auditlog.serviceImpl;

import com.clickkart.auditlog.dto.request.AuditEventRequest;
import com.clickkart.auditlog.entity.AuditChainHeadEntity;
import com.clickkart.auditlog.entity.AuditLogEntryEntity;
import com.clickkart.auditlog.repository.AuditChainHeadRepository;
import com.clickkart.auditlog.repository.AuditLogEntryRepository;
import com.clickkart.auditlog.service.AuditTrailService;
import com.clickkart.auditlog.service.ChainIntegrityReport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditLogEntryRepository auditLogEntryRepository;
    private final AuditChainHeadRepository auditChainHeadRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(AuditEventRequest request) {
        AuditChainHeadEntity head = auditChainHeadRepository
                .lockForUpdate(AuditChainHeadEntity.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Audit chain head row missing - AuditChainSeeder should have created it at startup"));

        AuditLogEntryEntity entry = AuditLogEntryEntity.create(
                request.timestamp(),
                request.correlationId(),
                request.actor(),
                request.action(),
                request.ipAddress(),
                request.details(),
                head.getLastEntryHash());
        auditLogEntryRepository.save(entry);

        head.advance(entry.getEntryHash());
        auditChainHeadRepository.save(head);
    }

    /** O(n) over the whole table; see {@code AuditLogEntryRepository.findAllByOrderByIdAsc} for the scaling caveat. */
    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ChainIntegrityReport verifyChainIntegrity() {
        List<AuditLogEntryEntity> entries = auditLogEntryRepository.findAllByOrderByIdAsc();

        String expectedPreviousHash = GENESIS_HASH;
        for (AuditLogEntryEntity entry : entries) {
            if (!expectedPreviousHash.equals(entry.getPreviousEntryHash())) {
                return ChainIntegrityReport.broken(
                        entries.size(), entry.getId(), "previousEntryHash does not match the prior entry's hash - chain link broken");
            }
            if (!entry.recomputeHash().equals(entry.getEntryHash())) {
                return ChainIntegrityReport.broken(
                        entries.size(), entry.getId(), "recomputed hash does not match the stored entryHash - entry may have been tampered with");
            }
            expectedPreviousHash = entry.getEntryHash();
        }

        return ChainIntegrityReport.intact(entries.size());
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Page<AuditLogEntryEntity> browse(Pageable pageable) {
        return auditLogEntryRepository.findAllByOrderByIdAsc(pageable);
    }
}
