-- V1__baseline.sql
-- Generated from the live schema Hibernate's ddl-auto produced, so this is exactly what already
-- exists rather than a hand-written approximation of it.
--
-- Existing databases are baselined at V1 and skip this file (spring.flyway.baseline-version=1).
-- A fresh database gets its whole schema from here - which is the point: the schema becomes a
-- reviewed artefact in git rather than a side effect of whatever the entity classes happened to
-- look like the last time the application started.


CREATE TABLE audit_chain_head (
    id bigint NOT NULL,
    created_date timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    entry_count bigint NOT NULL,
    last_entry_hash character varying(64) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL
);

CREATE TABLE audit_log_entries (
    id bigint NOT NULL,
    created_date timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    action character varying(60) NOT NULL,
    actor character varying(64) NOT NULL,
    correlation_id character varying(36) NOT NULL,
    details character varying(2000),
    entry_hash character varying(64) NOT NULL,
    ip_address character varying(45) NOT NULL,
    occurred_at timestamp(6) with time zone NOT NULL,
    previous_entry_hash character varying(64) NOT NULL
);

CREATE SEQUENCE audit_log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY audit_chain_head
    ADD CONSTRAINT audit_chain_head_pkey PRIMARY KEY (id);

ALTER TABLE ONLY audit_log_entries
    ADD CONSTRAINT audit_log_entries_pkey PRIMARY KEY (id);

CREATE INDEX idx_audit_log_entries_actor ON audit_log_entries USING btree (actor);

CREATE INDEX idx_audit_log_entries_correlation_id ON audit_log_entries USING btree (correlation_id);

CREATE INDEX idx_audit_log_entries_occurred_at ON audit_log_entries USING btree (occurred_at);

