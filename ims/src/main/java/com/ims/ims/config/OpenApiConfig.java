package com.ims.ims.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 (Swagger) Configuration for Inventory Management System (IMS).
 * Exposes interactive documentation at /swagger-ui.html and OpenAPI schema at /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI imsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Management System (IMS) REST API")
                        .description("Production-ready REST API for managing products, categories, suppliers, orders, inventory movements, and system telemetry.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Engineering Portfolio Team")
                                .email("developer@example.com")
                                .url("https://github.com/yourusername/ims"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}
