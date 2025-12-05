package com.destore.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Store Service.
 * <p>
 * Manages store locations and information for the DE-Store network.
 */
@SpringBootApplication
public class StoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreServiceApplication.class, args);
    }
}
