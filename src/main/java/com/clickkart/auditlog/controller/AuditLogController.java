// src/main/java/com/clickkart/auditlog/controller/AuditLogController.java
package com.clickkart.auditlog.controller;

import com.clickkart.auditlog.constant.ApiPaths;
import com.clickkart.auditlog.constant.MdcKeys;
import com.clickkart.auditlog.dto.ApiResponse;
import com.clickkart.auditlog.dto.PageResponse;
import com.clickkart.auditlog.dto.request.AuditEventRequest;
import com.clickkart.auditlog.dto.response.AuditLogEntryResponse;
import com.clickkart.auditlog.filter.CorrelationIdFilter;
import com.clickkart.auditlog.service.AuditTrailService;
import com.clickkart.auditlog.service.ChainIntegrityReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /events} is the write contract every other service's own audit-dispatch Feign
 * client calls (matches Auth Service's {@code AuditLogServiceClient.logEvent} exactly). {@code
 * GET /events}/{@code /events/verify} let an operator browse the trail and independently confirm
 * it hasn't been tampered with.
 *
 * <p>No RBAC on any of these endpoints yet - this service has no Gateway route or admin UI
 * reaching it directly today, only internal service-to-service traffic (private network only,
 * see {@code k8s/audit-log-service/service-and-scaling.yaml} - ClusterIP). Revisit once an
 * Admin Service/Gateway route exists to expose the read endpoints to an actual operator.
 */
@Tag(name = "Audit Log", description = "Central tamper-evident audit trail")
@RestController
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditTrailService auditTrailService;

    /** 200 OK, {@code data: null}. Matches Auth Service's {@code AuditLogServiceClient.logEvent}. */
    @Operation(summary = "Append one audit event to the chain")
    @PostMapping(ApiPaths.EVENTS)
    public ResponseEntity<ApiResponse<Void>> record(
            @RequestHeader(CorrelationIdFilter.CORRELATION_ID_HEADER) String correlationId,
            @Valid @RequestBody AuditEventRequest request,
            HttpServletRequest httpRequest) {
        auditTrailService.record(request);
        return envelope(HttpStatus.OK.value(), null, httpRequest);
    }

    /** 200 OK, {@code data}: a page of audit entries in chain order (oldest first). */
    @Operation(summary = "Browse the audit trail")
    @GetMapping(ApiPaths.EVENTS)
    public ResponseEntity<ApiResponse<PageResponse<AuditLogEntryResponse>>> browse(Pageable pageable, HttpServletRequest httpRequest) {
        Page<AuditLogEntryResponse> page = auditTrailService.browse(pageable).map(AuditLogEntryResponse::from);
        return envelope(HttpStatus.OK.value(), PageResponse.from(page), httpRequest);
    }

    /** 200 OK, {@code data}: whether the whole chain still checks out, or the id of the first entry that doesn't. */
    @Operation(summary = "Independently verify the audit hash chain hasn't been tampered with")
    @GetMapping(ApiPaths.EVENTS_VERIFY)
    public ResponseEntity<ApiResponse<ChainIntegrityReport>> verify(HttpServletRequest httpRequest) {
        ChainIntegrityReport report = auditTrailService.verifyChainIntegrity();
        return envelope(HttpStatus.OK.value(), report, httpRequest);
    }

    private <T> ResponseEntity<ApiResponse<T>> envelope(int status, T data, HttpServletRequest request) {
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        ApiResponse<T> body = ApiResponse.success(status, data, request.getRequestURI(), correlationId);
        return ResponseEntity.status(status).body(body);
    }
}
