package com.destore.inventory.service;

import com.destore.inventory.model.auth.User;
import com.destore.inventory.model.dto.StockAlert;
import com.destore.inventory.model.warehouse.WarehouseItem;
import com.destore.inventory.repository.auth.UserRepository;
import com.destore.inventory.repository.warehouse.WarehouseItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @file StockMonitoringService.java
 * @brief Service for monitoring warehouse stock levels and sending alerts
 * 
 * This service is responsible for the core inventory monitoring functionality:
 * - Scheduled stock level checks (default: every 6 hours)
 * - Detection of low, critical, and out-of-stock items
 * - Email notification to network managers
 * - Manual trigger support for on-demand checks
 * 
 * The service reads from two databases:
 * - Warehouse DB (MySQL): Item stock levels (read-only)
 * - Auth DB (PostgreSQL): Network manager email addresses (read-only)
 * 
 * Thresholds are dynamically configured via InventoryConfigService,
 * allowing runtime updates without service restart.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
public class StockMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(StockMonitoringService.class);
    
    private final WarehouseItemRepository warehouseItemRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final InventoryConfigService configService;

    public StockMonitoringService(
            WarehouseItemRepository warehouseItemRepository,
            UserRepository userRepository,
            EmailService emailService,
            InventoryConfigService configService) {
        this.warehouseItemRepository = warehouseItemRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.configService = configService;
    }

    /**
     * @brief Scheduled task to check stock levels and send alerts
     * 
     * This method runs automatically according to the cron expression configured
     * in application.yml (default: every 6 hours at minute 0).
     * 
     * Workflow:
     * 1. Query warehouse DB for items below low stock threshold
     * 2. Classify items by severity (out-of-stock, critical, low)
     * 3. Query auth DB for active network manager emails
     * 4. Send HTML email with grouped alerts
     * 5. Log results and completion
     * 
     * @throws Exception Catches all exceptions to prevent schedule disruption
     */
    @Scheduled(cron = "${inventory.schedule}")
    public void checkStockLevels() {
        log.info("Starting scheduled stock level check...");
        
        try {
            // Gather all stock alerts
            List<StockAlert> alerts = gatherStockAlerts();
            
            if (alerts.isEmpty()) {
                log.info("No stock alerts detected. All items have sufficient stock.");
                return;
            }
            
            log.info("Found {} stock alerts: {} out-of-stock, {} critical, {} low",
                alerts.size(),
                countByStatus(alerts, StockAlert.StockStatus.OUT_OF_STOCK),
                countByStatus(alerts, StockAlert.StockStatus.CRITICAL),
                countByStatus(alerts, StockAlert.StockStatus.LOW));
            
            // Get network manager email addresses
            List<String> networkManagerEmails = getNetworkManagerEmails();
            
            if (networkManagerEmails.isEmpty()) {
                log.warn("No active network managers found. Alerts will not be sent.");
                return;
            }
            
            // Send alert email
            emailService.sendStockAlerts(networkManagerEmails, alerts);
            
            log.info("Stock level check completed successfully");
            
        } catch (Exception e) {
            log.error("Error during stock level check", e);
        }
    }

    /**
     * @brief Gather all stock alerts by checking warehouse items
     * 
     * Queries the warehouse database for items below the configured low stock
     * threshold and classifies them into severity levels.
     * 
     * Classification Logic:
     * - OUT_OF_STOCK: stockQuantity == 0
     * - CRITICAL: 0 < stockQuantity <= criticalThreshold
     * - LOW: criticalThreshold < stockQuantity <= lowThreshold
     * 
     * @return List of StockAlert DTOs with item details and status
     */
    private List<StockAlert> gatherStockAlerts() {
        List<StockAlert> alerts = new ArrayList<>();
        
        // Get current thresholds from config service
        int lowThreshold = configService.getLowStockThreshold();
        int criticalThreshold = configService.getCriticalStockThreshold();
        
        // Find all items with low stock (includes critical and out-of-stock)
        List<WarehouseItem> lowStockItems = warehouseItemRepository.findLowStockItems(lowThreshold);
        
        for (WarehouseItem item : lowStockItems) {
            StockAlert alert = new StockAlert();
            alert.setItemId(item.getItemId());
            alert.setItemName(item.getName());
            alert.setCategory(item.getCategory());
            alert.setCurrentStock(item.getStockQuantity());
            
            // Determine alert status based on current thresholds
            if (item.isOutOfStock()) {
                alert.setStatus(StockAlert.StockStatus.OUT_OF_STOCK);
            } else if (item.isCriticalStock(criticalThreshold)) {
                alert.setStatus(StockAlert.StockStatus.CRITICAL);
            } else {
                alert.setStatus(StockAlert.StockStatus.LOW);
            }
            
            alerts.add(alert);
        }
        
        return alerts;
    }

    /**
     * @brief Get email addresses of all active network managers
     * 
     * Queries the auth database for users with:
     * - role = 'NETWORK_MANAGER'
     * - isActive = true
     * - email is not null/empty
     * 
     * @return List of email addresses to send alerts to
     */
    private List<String> getNetworkManagerEmails() {
        List<User> networkManagers = userRepository.findActiveNetworkManagers();
        
        return networkManagers.stream()
            .map(User::getEmail)
            .filter(email -> email != null && !email.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Count alerts by status
     */
    private long countByStatus(List<StockAlert> alerts, StockAlert.StockStatus status) {
        return alerts.stream()
            .filter(alert -> alert.getStatus() == status)
            .count();
    }

    /**
     * @brief Manual trigger for stock check (callable via REST endpoint)
     * 
     * Executes the same logic as the scheduled task but on-demand.
     * Used by Network Managers through the admin UI for immediate checks.
     * 
     * @see checkStockLevels() for workflow details
     */
    public void triggerManualCheck() {
        log.info("Manual stock check triggered");
        checkStockLevels();
    }

    /**
     * @brief Get current stock alerts without sending emails
     * 
     * Retrieves the current state of stock alerts for display purposes.
     * Does not trigger email notifications.
     * 
     * Used by:
     * - Admin dashboard to display current alerts
     * - REST API for querying alert status
     * 
     * @return List of current stock alerts
     */
    public List<StockAlert> getCurrentAlerts() {
        return gatherStockAlerts();
    }
}
