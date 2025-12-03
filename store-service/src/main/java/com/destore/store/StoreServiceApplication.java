package com.destore.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @file StoreServiceApplication.java
 * @brief Main application class for Store Service
 * 
 * Manages store locations and information for the DE-Store network
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class StoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreServiceApplication.class, args);
    }
}
