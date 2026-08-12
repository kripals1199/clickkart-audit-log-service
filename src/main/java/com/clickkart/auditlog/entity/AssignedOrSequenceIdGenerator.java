// src/main/java/com/clickkart/auditlog/entity/AssignedOrSequenceIdGenerator.java
package com.clickkart.auditlog.entity;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * Every {@link BaseEntity} subclass gets its id from the shared sequence, except {@link
 * AuditChainHeadEntity}, which is a fixed-id singleton row. Special-cased here by entity type
 * rather than by pre-assigning {@code id} in the constructor before persist() - a non-null id on
 * an entity that also carries {@code BaseEntity}'s {@code @Version} makes Hibernate's own
 * transient/detached determination treat it as an inconsistent "detached entity with an
 * uninitialized version" state and throw, since a real detached (already-persisted) entity would
 * never have a null version. Leaving {@code id} null until this generator runs keeps that
 * determination consistent for every entity, including this one - same fix already proven in
 * Auth Service's own copy of this exact class.
 */
public class AssignedOrSequenceIdGenerator extends SequenceStyleGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner) {
        if (owner instanceof AuditChainHeadEntity) {
            return AuditChainHeadEntity.SINGLETON_ID;
        }
        return super.generate(session, owner);
    }
}
