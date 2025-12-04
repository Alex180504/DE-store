package com.destore.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Inventory Service - Monitors warehouse stock levels and sends alerts
 * 
 * Features:
 * - Scheduled stock monitoring (every 6 hours by default)
 * - Low stock, critical stock, and out-of-stock detection
 * - Email notifications to all network managers
 * - Read-only access to warehouse database
 */
@SpringBootApplication
@EnableScheduling
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
