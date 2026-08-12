// src/main/java/com/clickkart/auditlog/config/OpenApiConfig.java
package com.clickkart.auditlog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI auditLogServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClickKart Audit Log Service")
                        .version("1.0.0")
                        .description("Central, tamper-evident (hash-chained) audit trail for every other service."));
    }
}
