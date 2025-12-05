package com.destore.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Inventory Monitoring Service.
 * <p>
 * This is the entry point for the inventory monitoring microservice.
 * It enables scheduled tasks for automated stock level checking and
 * email notifications to network managers.
 * <p>
 * Key Features:
 * <ul>
 * <li>Multi-datasource configuration (Warehouse MySQL + Auth PostgreSQL)</li>
 * <li>Scheduled stock monitoring (configurable via cron expression)</li>
 * <li>Email notifications for low/critical/out-of-stock items</li>
 * <li>JWT-secured admin API for manual triggers and threshold management</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
