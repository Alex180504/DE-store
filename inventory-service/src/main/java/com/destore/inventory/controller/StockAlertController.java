package com.destore.inventory.controller;

import com.destore.inventory.model.dto.StockAlert;
import com.destore.inventory.service.StockMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for inventory monitoring
 * Optional endpoints for manual triggers and status checks
 */
@RestController
@RequestMapping("/api/inventory")
public class StockAlertController {

    private final StockMonitoringService stockMonitoringService;

    public StockAlertController(StockMonitoringService stockMonitoringService) {
        this.stockMonitoringService = stockMonitoringService;
    }

    /**
     * Get current stock alerts without sending emails
     * GET /api/inventory/alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<StockAlert>> getCurrentAlerts() {
        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Manually trigger stock check and send emails
     * POST /api/inventory/check
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, String>> triggerManualCheck() {
        stockMonitoringService.triggerManualCheck();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Stock check triggered successfully"
        ));
    }

    /**
     * Health check endpoint
     * GET /api/inventory/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "inventory-service"
        ));
    }
}
