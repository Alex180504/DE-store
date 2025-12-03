package com.destore.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @file PricingServiceApplication.java
 * @brief Main application class for DE-Store Pricing Service
 * 
 * This Spring Boot application provides pricing management with hierarchical rules
 * supporting global (network-wide) and store-specific pricing with promotions.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class PricingServiceApplication {

    /**
     * @brief Main entry point for the Pricing Service application
     * 
     * Initializes the Spring Boot application context and starts the embedded
     * web server.
     * 
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(PricingServiceApplication.class, args);
    }
}
