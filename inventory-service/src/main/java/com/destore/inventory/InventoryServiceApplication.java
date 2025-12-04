package com.destore.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @file InventoryServiceApplication.java
 * @brief Main application class for Inventory Monitoring Service
 * 
 * This is the entry point for the inventory monitoring microservice.
 * It enables scheduled tasks for automated stock level checking and
 * email notifications to network managers.
 * 
 * Key Features:
 * - Multi-datasource configuration (Warehouse MySQL + Auth PostgreSQL)
 * - Scheduled stock monitoring (configurable via cron expression)
 * - Email notifications for low/critical/out-of-stock items
 * - JWT-secured admin API for manual triggers and threshold management
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
