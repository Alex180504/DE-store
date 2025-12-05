package com.destore.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Loyalty Service.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class LoyaltyServiceApplication {

    /**
     * Application entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(LoyaltyServiceApplication.class, args);
    }
}
