// src/main/java/com/clickkart/auditlog/entity/BaseEntity.java
package com.clickkart.auditlog.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * {@code id}/{@code version} are strictly framework-managed - never set directly by application
 * code, even {@link AuditChainHeadEntity}'s fixed singleton id (see {@link
 * AssignedOrSequenceIdGenerator}'s Javadoc for why a pre-assigned id on a versioned entity
 * breaks Hibernate's own transient/detached determination - lesson learned building Auth
 * Service's own copy of this exact pattern).
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(generator = "audit_log_seq_gen")
    @GenericGenerator(
            name = "audit_log_seq_gen",
            type = AssignedOrSequenceIdGenerator.class,
            parameters = {
                @Parameter(name = "sequence_name", value = "audit_log_seq"),
                @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
