package com.destore.shopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Shopping Mock Service Application
 * <p>
 * Mock shopping service for testing loyalty system integration.
 * Provides a simple UI for creating baskets, viewing loyalty offers,
 * and testing the SAGA-based checkout flow with points redemption.
 * </p>
 *
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ShoppingMockApplication {

    /** 
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(ShoppingMockApplication.class, args);
    }

    /**
     * WebClient bean for making HTTP requests to loyalty service
     *
     * @return configured WebClient instance
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
