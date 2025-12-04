package com.destore.inventory.controller;

import com.destore.inventory.model.dto.StockAlert;
import com.destore.inventory.model.dto.ThresholdConfig;
import com.destore.inventory.model.dto.ThresholdUpdateRequest;
import com.destore.inventory.service.InventoryConfigService;
import com.destore.inventory.service.StockMonitoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Controller for Inventory Management
 * Requires NETWORK_MANAGER role
 */
@RestController
@RequestMapping("/api/inventory/admin")
public class InventoryAdminController {

    private final StockMonitoringService stockMonitoringService;
    private final InventoryConfigService configService;

    public InventoryAdminController(
            StockMonitoringService stockMonitoringService,
            InventoryConfigService configService) {
        this.stockMonitoringService = stockMonitoringService;
        this.configService = configService;
    }

    /**
     * Manually trigger stock check and send emails
     * POST /api/inventory/admin/trigger-check
     * Requires: NETWORK_MANAGER role
     */
    @PostMapping("/trigger-check")
    @PreAuthorize("hasRole('NETWORK_MANAGER')")
    public ResponseEntity<Map<String, Object>> triggerStockCheck() {
        stockMonitoringService.triggerManualCheck();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Stock check triggered successfully. Email alerts will be sent if any issues are detected.",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Get current stock alerts without sending emails
     * GET /api/inventory/admin/alerts
     * Requires: NETWORK_MANAGER role
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('NETWORK_MANAGER')")
    public ResponseEntity<List<StockAlert>> getCurrentAlerts() {
        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get current threshold configuration
     * GET /api/inventory/admin/thresholds
     * Requires: NETWORK_MANAGER role
     */
    @GetMapping("/thresholds")
    @PreAuthorize("hasRole('NETWORK_MANAGER')")
    public ResponseEntity<ThresholdConfig> getThresholds() {
        ThresholdConfig config = configService.getThresholds();
        return ResponseEntity.ok(config);
    }

    /**
     * Update threshold configuration
     * PUT /api/inventory/admin/thresholds
     * Requires: NETWORK_MANAGER role
     */
    @PutMapping("/thresholds")
    @PreAuthorize("hasRole('NETWORK_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateThresholds(
            @Valid @RequestBody ThresholdUpdateRequest request) {
        
        // Validate threshold logic
        if (request.getCriticalStock() >= request.getLowStock()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Critical stock threshold must be less than low stock threshold"
            ));
        }
        
        if (request.getOutOfStock() != 0) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Out of stock threshold must be 0"
            ));
        }
        
        configService.updateThresholds(
            request.getLowStock(),
            request.getCriticalStock(),
            request.getOutOfStock()
        );
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Thresholds updated successfully",
            "thresholds", configService.getThresholds()
        ));
    }

    /**
     * Get inventory monitoring statistics
     * GET /api/inventory/admin/stats
     * Requires: NETWORK_MANAGER role
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('NETWORK_MANAGER')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();
        
        long outOfStock = alerts.stream()
            .filter(a -> a.getStatus() == StockAlert.StockStatus.OUT_OF_STOCK)
            .count();
        long critical = alerts.stream()
            .filter(a -> a.getStatus() == StockAlert.StockStatus.CRITICAL)
            .count();
        long low = alerts.stream()
            .filter(a -> a.getStatus() == StockAlert.StockStatus.LOW)
            .count();
        
        return ResponseEntity.ok(Map.of(
            "totalAlerts", alerts.size(),
            "outOfStock", outOfStock,
            "critical", critical,
            "low", low,
            "thresholds", configService.getThresholds()
        ));
    }
}
