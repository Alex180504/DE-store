package com.destore.pricing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @file OpenApiConfig.java
 * @brief OpenAPI/Swagger configuration for API documentation
 * 
 * Configures Swagger UI and OpenAPI specification for the Pricing Service REST API.
 * Provides interactive API documentation accessible at /swagger-ui.html
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * @brief Configures OpenAPI specification metadata
     * 
     * Defines API information, contact details, and available servers for
     * the Swagger documentation interface.
     * 
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI pricingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DE-Store Pricing Service API")
                        .description("Microservice for hierarchical pricing management with global and store-specific rules")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DE-Store Team")
                                .email("support@destore.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("API Gateway (via Nginx)")
                ));
    }
}
