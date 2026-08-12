// src/main/java/com/clickkart/auditlog/filter/CorrelationIdFilter.java
package com.clickkart.auditlog.filter;

import com.clickkart.auditlog.constant.MdcKeys;
import com.clickkart.auditlog.exception.MissingCorrelationIdException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * This service is purely a correlation-id *receiver* (Rule 13: only Auth Service mints one) -
 * every inbound request must already carry {@code X-Correlation-Id}, set by Auth Service's
 * {@code AuditLogServiceClient} today. A missing/blank header is rejected outright ({@link
 * MissingCorrelationIdException} -> 400). Health/Swagger/actuator paths are exempt.
 */
@RequiredArgsConstructor
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final List<String> exemptPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isExempt(request.getServletPath())) {
            chain.doFilter(request, response);
            return;
        }

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            handlerExceptionResolver.resolveException(
                    request, response, null,
                    new MissingCorrelationIdException("Request is missing the required X-Correlation-Id header"));
            return;
        }

        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
        }
    }

    private boolean isExempt(String path) {
        for (String pattern : exemptPaths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
