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
 * Service for monitoring warehouse stock levels and sending alerts
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
     * Scheduled task to check stock levels and send alerts
     * Runs according to cron expression in application.yml (default: every 6 hours)
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
     * Gather all stock alerts by checking warehouse items
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
     * Get email addresses of all active network managers
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
     * Manual trigger for stock check (can be called via REST endpoint)
     */
    public void triggerManualCheck() {
        log.info("Manual stock check triggered");
        checkStockLevels();
    }

    /**
     * Get current stock alerts without sending emails
     */
    public List<StockAlert> getCurrentAlerts() {
        return gatherStockAlerts();
    }
}
