// src/main/java/com/clickkart/auditlog/config/AuditChainSeeder.java
package com.clickkart.auditlog.config;

import com.clickkart.auditlog.entity.AuditChainHeadEntity;
import com.clickkart.auditlog.repository.AuditChainHeadRepository;
import com.clickkart.auditlog.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the singleton {@link AuditChainHeadEntity} row exists before any request can try to
 * append to the audit hash chain - idempotent (checked via {@code findById} first), safe on
 * every boot across every replica. No Flyway/Liquibase in this project (locked decision) - this
 * is the seed-migration substitute, same pattern as Auth Service's own copy.
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditChainSeeder implements ApplicationRunner {

    private final AuditChainHeadRepository auditChainHeadRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        if (auditChainHeadRepository.findById(AuditChainHeadEntity.SINGLETON_ID).isEmpty()) {
            auditChainHeadRepository.save(new AuditChainHeadEntity(AuditTrailService.GENESIS_HASH));
        }
    }
}
