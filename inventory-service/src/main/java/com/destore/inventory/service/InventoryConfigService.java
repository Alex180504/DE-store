package com.destore.inventory.service;

import com.destore.inventory.model.dto.ThresholdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for managing inventory configuration
 * Note: In production, this should persist to a database
 * Currently using in-memory storage with environment variable defaults
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
     * Get current threshold configuration
     */
    public ThresholdConfig getThresholds() {
        initializeIfNeeded();
        return new ThresholdConfig(lowStockThreshold, criticalStockThreshold, outOfStockThreshold);
    }

    /**
     * Update threshold configuration
     */
    public synchronized void updateThresholds(int lowStock, int criticalStock, int outOfStock) {
        log.info("Updating thresholds: low={}, critical={}, out={}", 
            lowStock, criticalStock, outOfStock);
        
        this.lowStockThreshold = lowStock;
        this.criticalStockThreshold = criticalStock;
        this.outOfStockThreshold = outOfStock;
        
        log.info("Thresholds updated successfully");
    }

    /**
     * Get low stock threshold
     */
    public int getLowStockThreshold() {
        initializeIfNeeded();
        return lowStockThreshold;
    }

    /**
     * Get critical stock threshold
     */
    public int getCriticalStockThreshold() {
        initializeIfNeeded();
        return criticalStockThreshold;
    }

    /**
     * Get out of stock threshold
     */
    public int getOutOfStockThreshold() {
        initializeIfNeeded();
        return outOfStockThreshold;
    }

    /**
     * Initialize thresholds from environment variables on first use
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
