package com.destore.analytics.controller;

import com.destore.analytics.dto.StoreComparisonDTO;
import com.destore.analytics.repository.StoreAnalyticsRepository;
import com.destore.analytics.service.StoreAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for store comparison and network analytics endpoints.
 */
@RestController
@RequestMapping("/api/analytics/stores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Store Analytics", description = "Store comparison and network performance endpoints")
public class StoreAnalyticsController {

    private final StoreAnalyticsService service;

    /** 
     * @return ResponseEntity<List<StoreInfo>>
     */
    @GetMapping("/list")
    @Operation(summary = "Get all stores", 
               description = "Get list of all active stores for dropdown selection")
    public ResponseEntity<List<StoreAnalyticsRepository.StoreInfo>> getAllStores() {
        log.info("GET /api/analytics/stores/list");
        List<StoreAnalyticsRepository.StoreInfo> stores = service.getAllStores();
        return ResponseEntity.ok(stores);
    }

    /** 
     * @param getStoreComparison(
     * @return ResponseEntity<StoreComparisonDTO>
     */
    @GetMapping("/comparison")
    @Operation(summary = "Get store comparison", 
               description = "Compare performance across all stores in the network with rankings and growth rates")
    public ResponseEntity<StoreComparisonDTO> getStoreComparison(
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/analytics/stores/comparison - startDate: {}, endDate: {}", startDate, endDate);

        StoreComparisonDTO comparison = service.generateStoreComparison(startDate, endDate);
        return ResponseEntity.ok(comparison);
    }

    /** 
     * @return ResponseEntity<StoreComparisonDTO>
     */
    @GetMapping("/comparison/week")
    @Operation(summary = "Get current week's store comparison")
    public ResponseEntity<StoreComparisonDTO> getWeekStoreComparison() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        log.info("GET /api/analytics/stores/comparison/week");

        StoreComparisonDTO comparison = service.generateStoreComparison(weekStart, today);
        return ResponseEntity.ok(comparison);
    }

    /** 
     * @return ResponseEntity<StoreComparisonDTO>
     */
    @GetMapping("/comparison/month")
    @Operation(summary = "Get current month's store comparison")
    public ResponseEntity<StoreComparisonDTO> getMonthStoreComparison() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        log.info("GET /api/analytics/stores/comparison/month");

        StoreComparisonDTO comparison = service.generateStoreComparison(monthStart, today);
        return ResponseEntity.ok(comparison);
    }

    /** 
     * @param getStorePerformance(
     * @return ResponseEntity<StoreMetrics>
     */
    @GetMapping("/{storeId}/performance")
    @Operation(summary = "Get individual store performance", 
               description = "Get detailed performance metrics for a specific store")
    public ResponseEntity<StoreComparisonDTO.StoreMetrics> getStorePerformance(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/analytics/stores/{}/performance - startDate: {}, endDate: {}", storeId, startDate, endDate);

        StoreComparisonDTO.StoreMetrics metrics = service.getStorePerformance(storeId, startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
}
