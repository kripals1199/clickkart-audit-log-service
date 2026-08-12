// src/main/java/com/clickkart/auditlog/dto/ErrorDetail.java
package com.clickkart.auditlog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(String code, Map<String, String> fieldErrors) {

    public static ErrorDetail of(String code) {
        return new ErrorDetail(code, null);
    }

    public static ErrorDetail withFieldErrors(String code, Map<String, String> fieldErrors) {
        return new ErrorDetail(code, fieldErrors);
    }
}
