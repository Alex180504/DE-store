package com.destore.analytics.controller;

import com.destore.analytics.dto.ProductPerformanceDTO;
import com.destore.analytics.service.ProductAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for product performance analytics endpoints.
 */
@RestController
@RequestMapping("/api/analytics/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Analytics", description = "Product performance and category analysis endpoints")
public class ProductAnalyticsController {

    private final ProductAnalyticsService service;

    @GetMapping("/performance")
    @Operation(summary = "Get product performance", 
               description = "Generate product performance report including top sellers and category analysis")
    public ResponseEntity<ProductPerformanceDTO> getProductPerformance(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Number of top products to return (1-100, default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        log.info("GET /api/analytics/products/performance - storeId: {}, startDate: {}, endDate: {}, limit: {}", 
            storeId, startDate, endDate, limit);

        ProductPerformanceDTO performance = service.generateProductPerformance(storeId, startDate, endDate, limit);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/performance/week")
    @Operation(summary = "Get current week's product performance")
    public ResponseEntity<ProductPerformanceDTO> getWeekProductPerformance(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId,
            
            @Parameter(description = "Number of top products to return (1-100, default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        log.info("GET /api/analytics/products/performance/week - storeId: {}, limit: {}", storeId, limit);

        ProductPerformanceDTO performance = service.generateProductPerformance(storeId, weekStart, today, limit);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/performance/month")
    @Operation(summary = "Get current month's product performance")
    public ResponseEntity<ProductPerformanceDTO> getMonthProductPerformance(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId,
            
            @Parameter(description = "Number of top products to return (1-100, default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        log.info("GET /api/analytics/products/performance/month - storeId: {}, limit: {}", storeId, limit);

        ProductPerformanceDTO performance = service.generateProductPerformance(storeId, monthStart, today, limit);
        return ResponseEntity.ok(performance);
    }
}
