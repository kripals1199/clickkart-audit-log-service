// src/main/java/com/clickkart/auditlog/constant/ApiPaths.java
package com.clickkart.auditlog.constant;

/** Single source of truth for this service's route strings. */
public final class ApiPaths {

    private ApiPaths() {}

    public static final String BASE = "/api/v1/audit-log";

    public static final String EVENTS = BASE + "/events";
    public static final String EVENTS_VERIFY = EVENTS + "/verify";

    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_HEALTH_WILDCARD = "/actuator/health/**";
    public static final String ACTUATOR_PROMETHEUS = "/actuator/prometheus";
    public static final String SWAGGER_UI = "/swagger-ui.html";
    public static final String SWAGGER_UI_WILDCARD = "/swagger-ui/**";
    public static final String API_DOCS_WILDCARD = "/v3/api-docs/**";
}
