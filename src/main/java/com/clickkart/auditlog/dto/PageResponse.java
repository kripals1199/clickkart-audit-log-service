// src/main/java/com/clickkart/auditlog/dto/PageResponse.java
package com.clickkart.auditlog.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Lean pagination contract - just the fields a caller actually needs, not Spring Data's full {@code Page<T>} serialization. */
public record PageResponse<T>(
        List<T> content, int page, int size, long totalElements, int totalPages, boolean first, boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
