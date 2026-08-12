// src/test/java/com/clickkart/auditlog/serviceImpl/AuditTrailServiceImplTest.java
package com.clickkart.auditlog.serviceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clickkart.auditlog.dto.request.AuditEventRequest;
import com.clickkart.auditlog.entity.AuditChainHeadEntity;
import com.clickkart.auditlog.entity.AuditLogEntryEntity;
import com.clickkart.auditlog.repository.AuditChainHeadRepository;
import com.clickkart.auditlog.repository.AuditLogEntryRepository;
import com.clickkart.auditlog.service.AuditTrailService;
import com.clickkart.auditlog.service.ChainIntegrityReport;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuditTrailServiceImplTest {

    @Mock
    private AuditLogEntryRepository auditLogEntryRepository;

    @Mock
    private AuditChainHeadRepository auditChainHeadRepository;

    private AuditTrailServiceImpl auditTrailService;

    @BeforeEach
    void setUp() {
        auditTrailService = new AuditTrailServiceImpl(auditLogEntryRepository, auditChainHeadRepository);
    }

    @Test
    void recordAppendsEntryAndAdvancesChainHead() {
        AuditChainHeadEntity head = new AuditChainHeadEntity(AuditTrailService.GENESIS_HASH);
        when(auditChainHeadRepository.lockForUpdate(AuditChainHeadEntity.SINGLETON_ID)).thenReturn(Optional.of(head));

        AuditEventRequest request = new AuditEventRequest(
                "correlation-id-1", "USR-123", "REGISTER", "127.0.0.1", Instant.now(), null);

        auditTrailService.record(request);

        ArgumentCaptor<AuditLogEntryEntity> entryCaptor = ArgumentCaptor.forClass(AuditLogEntryEntity.class);
        verify(auditLogEntryRepository).save(entryCaptor.capture());
        assertThat(entryCaptor.getValue().getCorrelationId()).isEqualTo("correlation-id-1");
        assertThat(entryCaptor.getValue().getPreviousEntryHash()).isEqualTo(AuditTrailService.GENESIS_HASH);

        assertThat(head.getEntryCount()).isEqualTo(1);
        assertThat(head.getLastEntryHash()).isEqualTo(entryCaptor.getValue().getEntryHash());
        verify(auditChainHeadRepository).save(head);
    }

    @Test
    void verifyChainIntegrityReportsIntactForAnEmptyChain() {
        when(auditLogEntryRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        ChainIntegrityReport report = auditTrailService.verifyChainIntegrity();

        assertThat(report.intact()).isTrue();
        assertThat(report.entriesChecked()).isZero();
    }

    @Test
    void verifyChainIntegrityDetectsATamperedEntry() {
        AuditLogEntryEntity entry = AuditLogEntryEntity.create(
                Instant.now(), "correlation-id-1", "USR-123", "REGISTER", "127.0.0.1", null, AuditTrailService.GENESIS_HASH);
        ReflectionTestUtils.setField(entry, "id", 1L);
        // Simulate tampering: mutate a field after the hash was computed, without recomputing entryHash.
        ReflectionTestUtils.setField(entry, "actor", "USR-999-TAMPERED");

        when(auditLogEntryRepository.findAllByOrderByIdAsc()).thenReturn(List.of(entry));

        ChainIntegrityReport report = auditTrailService.verifyChainIntegrity();

        assertThat(report.intact()).isFalse();
        assertThat(report.brokenAtEntryId()).isEqualTo(1L);
    }

    @Test
    void browseDelegatesToRepository() {
        when(auditLogEntryRepository.findAllByOrderByIdAsc(any())).thenReturn(org.springframework.data.domain.Page.empty());

        auditTrailService.browse(org.springframework.data.domain.Pageable.unpaged());

        verify(auditLogEntryRepository).findAllByOrderByIdAsc(any());
    }
}
