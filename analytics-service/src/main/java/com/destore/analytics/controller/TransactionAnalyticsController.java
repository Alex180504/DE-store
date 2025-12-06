package com.destore.analytics.controller;

import com.destore.analytics.dto.TransactionSummaryDTO;
import com.destore.analytics.service.TransactionAnalyticsService;
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
 * REST controller for transaction analytics endpoints.
 */
@RestController
@RequestMapping("/api/analytics/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Analytics", description = "Transaction volume and status analysis endpoints")
public class TransactionAnalyticsController {

    private final TransactionAnalyticsService service;

    /** 
     * @param getTransactionSummary(
     * @return ResponseEntity<TransactionSummaryDTO>
     */
    @GetMapping("/summary")
    @Operation(summary = "Get transaction summary", 
               description = "Generate comprehensive transaction summary including status breakdown and hourly distribution")
    public ResponseEntity<TransactionSummaryDTO> getTransactionSummary(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/analytics/transactions/summary - storeId: {}, startDate: {}, endDate: {}", 
            storeId, startDate, endDate);

        TransactionSummaryDTO summary = service.generateTransactionSummary(storeId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /** 
     * @param getTodayTransactions(
     * @return ResponseEntity<TransactionSummaryDTO>
     */
    @GetMapping("/summary/today")
    @Operation(summary = "Get today's transaction summary")
    public ResponseEntity<TransactionSummaryDTO> getTodayTransactions(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId) {
        
        LocalDate today = LocalDate.now();
        log.info("GET /api/analytics/transactions/summary/today - storeId: {}", storeId);

        TransactionSummaryDTO summary = service.generateTransactionSummary(storeId, today, today);
        return ResponseEntity.ok(summary);
    }

    /** 
     * @param getWeekTransactions(
     * @return ResponseEntity<TransactionSummaryDTO>
     */
    @GetMapping("/summary/week")
    @Operation(summary = "Get current week's transaction summary")
    public ResponseEntity<TransactionSummaryDTO> getWeekTransactions(
            @Parameter(description = "Store ID", required = true)
            @RequestParam Long storeId) {
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        log.info("GET /api/analytics/transactions/summary/week - storeId: {}", storeId);

        TransactionSummaryDTO summary = service.generateTransactionSummary(storeId, weekStart, today);
        return ResponseEntity.ok(summary);
    }
}
