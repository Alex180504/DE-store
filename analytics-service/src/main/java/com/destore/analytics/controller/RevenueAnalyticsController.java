package com.destore.analytics.controller;

import com.destore.analytics.dto.RevenueReportDTO;
import com.destore.analytics.service.RevenueAnalyticsService;
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
 * REST controller for revenue analytics endpoints.
 */
@RestController
@RequestMapping("/api/analytics/revenue")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Revenue Analytics", description = "Revenue reporting and analysis endpoints")
public class RevenueAnalyticsController {

    private final RevenueAnalyticsService service;

    @GetMapping("/summary")
    @Operation(summary = "Get revenue summary", 
               description = "Generate comprehensive revenue report for a store including daily breakdown and payment method analysis")
    public ResponseEntity<RevenueReportDTO> getRevenueSummary(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/analytics/revenue/summary - storeId: {}, startDate: {}, endDate: {}", 
            storeId, startDate, endDate);

        RevenueReportDTO report = service.generateRevenueReport(storeId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary/today")
    @Operation(summary = "Get today's revenue summary")
    public ResponseEntity<RevenueReportDTO> getTodayRevenue(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId) {
        
        LocalDate today = LocalDate.now();
        log.info("GET /api/analytics/revenue/summary/today - storeId: {}", storeId);

        RevenueReportDTO report = service.generateRevenueReport(storeId, today, today);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary/week")
    @Operation(summary = "Get current week's revenue summary")
    public ResponseEntity<RevenueReportDTO> getWeekRevenue(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId) {
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        log.info("GET /api/analytics/revenue/summary/week - storeId: {}", storeId);

        RevenueReportDTO report = service.generateRevenueReport(storeId, weekStart, today);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary/month")
    @Operation(summary = "Get current month's revenue summary")
    public ResponseEntity<RevenueReportDTO> getMonthRevenue(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId) {
        
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        log.info("GET /api/analytics/revenue/summary/month - storeId: {}", storeId);

        RevenueReportDTO report = service.generateRevenueReport(storeId, monthStart, today);
        return ResponseEntity.ok(report);
    }
}
