// src/main/java/com/clickkart/auditlog/exception/MissingCorrelationIdException.java
package com.clickkart.auditlog.exception;

/** Thrown when an inbound request has no {@code X-Correlation-Id} header (Rule 13). */
public class MissingCorrelationIdException extends RuntimeException {

    public MissingCorrelationIdException(String message) {
        super(message);
    }
}
