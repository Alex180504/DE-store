package com.destore.inventory.service;

import com.destore.inventory.model.dto.ThresholdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @file InventoryConfigService.java
 * @brief Service for managing inventory threshold configuration
 * 
 * This service provides runtime configuration management for stock alert thresholds.
 * Thresholds determine when items are flagged as low, critical, or out of stock.
 * 
 * Current Implementation:
 * - In-memory storage (volatile - resets on service restart)
 * - Initialized from environment variables
 * - Thread-safe updates with synchronized methods
 * - Network managers can update via admin API
 * 
 * Configuration Parameters:
 * - lowStockThreshold: Items below this trigger LOW alerts (default: 50)
 * - criticalStockThreshold: Items below this trigger CRITICAL alerts (default: 20)
 * - outOfStockThreshold: Items at this level trigger OUT_OF_STOCK alerts (default: 0)
 * 
 * @note Production TODO: Persist configuration to database table
 * @note Production TODO: Track configuration change history
 * @note Production TODO: Support per-category thresholds
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
public class InventoryConfigService {

    private static final Logger log = LoggerFactory.getLogger(InventoryConfigService.class);

    @Value("${inventory.thresholds.low-stock}")
    private int defaultLowStock;
    
    @Value("${inventory.thresholds.critical-stock}")
    private int defaultCriticalStock;
    
    @Value("${inventory.thresholds.out-of-stock}")
    private int defaultOutOfStock;

    // In-memory storage (volatile - resets on restart)
    // TODO: Persist to database for production use
    private volatile int lowStockThreshold;
    private volatile int criticalStockThreshold;
    private volatile int outOfStockThreshold;
    private boolean initialized = false;

    /**
     * @brief Get current threshold configuration
     * 
     * Returns threshold values as a DTO for API responses.
     * Initializes from environment variables on first call if needed.
     * Note: Out-of-stock is always 0 and not configurable.
     * 
     * @return ThresholdConfig DTO with current values
     */
    public ThresholdConfig getThresholds() {
        initializeIfNeeded();
        return new ThresholdConfig(lowStockThreshold, criticalStockThreshold);
    }

    /**
     * @brief Update threshold configuration
     * 
     * Updates configurable thresholds atomically in memory.
     * Changes take effect immediately for the next stock check.
     * Out-of-stock threshold is always 0 and cannot be changed.
     * 
     * Thread Safety: Method is synchronized to prevent race conditions
     * during concurrent updates.
     * 
     * @param lowStock New low stock threshold
     * @param criticalStock New critical stock threshold
     * 
     * @note Changes are lost on service restart (in-memory only)
     * @note Should be enhanced to persist to database in production
     */
    public synchronized void updateThresholds(int lowStock, int criticalStock) {
        log.info("Updating thresholds: low={}, critical={}", lowStock, criticalStock);
        
        this.lowStockThreshold = lowStock;
        this.criticalStockThreshold = criticalStock;
        // outOfStockThreshold remains 0 (hardcoded)
        
        log.info("Thresholds updated successfully");
    }

    /**
     * @brief Get low stock threshold
     * 
     * Returns the current threshold for triggering LOW stock alerts.
     * Items with stock quantity between critical and low thresholds
     * are flagged as low stock.
     * 
     * @return Low stock threshold value
     */
    public int getLowStockThreshold() {
        initializeIfNeeded();
        return lowStockThreshold;
    }

    /**
     * @brief Get critical stock threshold
     * 
     * Returns the current threshold for triggering CRITICAL stock alerts.
     * Items with stock quantity between 0 and critical threshold (exclusive)
     * are flagged as critical.
     * 
     * @return Critical stock threshold value
     */
    public int getCriticalStockThreshold() {
        initializeIfNeeded();
        return criticalStockThreshold;
    }

    /**
     * @brief Get out of stock threshold
     * 
     * Returns the threshold for triggering OUT_OF_STOCK alerts.
     * This is always 0 (items with exactly 0 stock).
     * 
     * @return Out of stock threshold (always 0)
     */
    public int getOutOfStockThreshold() {
        initializeIfNeeded();
        return outOfStockThreshold;
    }

    /**
     * @brief Initialize thresholds from environment variables on first use
     * 
     * Lazy initialization pattern - only runs once on first getter call.
     * Loads default values from application.yml via @Value annotations.
     * 
     * Thread Safety: Method is synchronized to prevent double initialization
     * in concurrent scenarios.
     */
    private synchronized void initializeIfNeeded() {
        if (!initialized) {
            lowStockThreshold = defaultLowStock;
            criticalStockThreshold = defaultCriticalStock;
            outOfStockThreshold = defaultOutOfStock;
            initialized = true;
            log.info("Initialized thresholds from environment: low={}, critical={}, out={}", 
                lowStockThreshold, criticalStockThreshold, outOfStockThreshold);
        }
    }
}
