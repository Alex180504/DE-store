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
 * REST controller for inventory administration endpoints.
 * <p>
 * This controller provides management APIs for Network Managers to:
 * <ul>
 *   <li>Trigger manual stock checks</li>
 *   <li>View current stock alerts</li>
 *   <li>Configure alert thresholds</li>
 *   <li>Monitor inventory statistics</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Security:</strong>
 * <ul>
 *   <li>All admin endpoints require JWT authentication with NETWORK_MANAGER role.</li>
 *   <li>Role validation is enforced via {@code @PreAuthorize} annotations.</li>
 *   <li>Health check endpoint is public (no authentication required).</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Endpoints:</strong>
 * <ul>
 *   <li>GET  /api/inventory/health - Public health check</li>
 *   <li>POST /api/inventory/admin/trigger-check - Manual stock check with email</li>
 *   <li>GET  /api/inventory/admin/alerts - Current alerts (no email)</li>
 *   <li>GET  /api/inventory/admin/thresholds - Get threshold configuration</li>
 *   <li>PUT  /api/inventory/admin/thresholds - Update thresholds</li>
 *   <li>GET  /api/inventory/admin/stats - Statistics dashboard data</li>
 * </ul>
 * </p>
 *
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryAdminController {

    private final StockMonitoringService stockMonitoringService;
    private final InventoryConfigService configService;

    /**
     * Constructs a new InventoryAdminController.
     *
     * @param stockMonitoringService the service for stock monitoring operations
     * @param configService          the service for inventory configuration
     */
    public InventoryAdminController(
            StockMonitoringService stockMonitoringService,
            InventoryConfigService configService) {
        this.stockMonitoringService = stockMonitoringService;
        this.configService = configService;
    }

    /**
     * Health check endpoint (public).
     * <p>
     * Simple health check to verify service is running.
     * No authentication required - accessible to all.
     * </p>
     * <p>
     * <strong>HTTP Method:</strong> GET<br>
     * <strong>Path:</strong> /api/inventory/health<br>
     * <strong>Authorization:</strong> None (public endpoint)
     * </p>
     *
     * @return JSON with status and service name
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "inventory-service"
        ));
    }

    /**
     * Manually triggers stock check and sends emails.
     * <p>
     * Endpoint for Network Managers to trigger an immediate stock check
     * outside of the regular schedule. Useful for:
     * <ul>
     *   <li>Testing alert system</li>
     *   <li>Immediate checks after restocking</li>
     *   <li>On-demand reports</li>
     * </ul>
     * </p>
     * <p>
     * <strong>HTTP Method:</strong> POST<br>
     * <strong>Path:</strong> /api/inventory/admin/trigger-check<br>
     * <strong>Authorization:</strong> JWT with NETWORK_MANAGER role required
     * </p>
     *
     * @return JSON with status, message, and timestamp
     */
    @PostMapping("/admin/trigger-check")
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
     * @brief Get current stock alerts without sending emails
     * 
     * Returns the current state of stock alerts for dashboard display.
     * Does not trigger email notifications - use for UI display only.
     * 
     * HTTP Method: GET
     * Path: /api/inventory/admin/alerts
     * Authorization: JWT with NETWORK_MANAGER role required
     * 
     * @return JSON array of StockAlert objects
     */
    @GetMapping("/admin/alerts")
    @PreAuthorize("hasRole('NETWORK_MANAGER')")
    public ResponseEntity<List<StockAlert>> getCurrentAlerts() {
        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * @brief Get current threshold configuration
     * 
     * Returns the current alert thresholds for display in the admin UI.
     * Used to populate the threshold configuration form.
     * 
     * HTTP Method: GET
     * Path: /api/inventory/admin/thresholds
     * Authorization: JWT with NETWORK_MANAGER role required
     * 
     * @return JSON object with lowStock, criticalStock values
     */
    @GetMapping("/admin/thresholds")
    @PreAuthorize("hasRole('NETWORK_MANAGER')")
    public ResponseEntity<ThresholdConfig> getThresholds() {
        ThresholdConfig config = configService.getThresholds();
        return ResponseEntity.ok(config);
    }

    /**
     * @brief Update threshold configuration
     * 
     * Allows Network Managers to modify alert thresholds via the admin UI.
     * Changes take effect immediately for subsequent stock checks.
     * Out-of-stock threshold is always 0 (hardcoded).
     * 
     * Validation:
     * - criticalStock must be less than lowStock
     * 
     * HTTP Method: PUT
     * Path: /api/inventory/admin/thresholds
     * Authorization: JWT with NETWORK_MANAGER role required
     * Content-Type: application/json
     * 
     * @param request ThresholdUpdateRequest with new values
     * @return JSON with status, message, and updated thresholds
     */
    @PutMapping("/admin/thresholds")
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
        
        configService.updateThresholds(
            request.getLowStock(),
            request.getCriticalStock()
        );
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Thresholds updated successfully",
            "thresholds", configService.getThresholds()
        ));
    }

    /**
     * @brief Get inventory monitoring statistics
     * 
     * Returns comprehensive dashboard data including:
     * - Total number of alerts
     * - Count by severity (out-of-stock, critical, low)
     * - Current threshold configuration
     * 
     * Used by the admin UI to populate the statistics cards
     * and provide an overview of inventory health.
     * 
     * HTTP Method: GET
     * Path: /api/inventory/admin/stats
     * Authorization: JWT with NETWORK_MANAGER role required
     * 
     * @return JSON with totalAlerts, outOfStock, critical, low counts, and thresholds
     */
    @GetMapping("/admin/stats")
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
