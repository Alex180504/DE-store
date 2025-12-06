package com.destore.analytics.service;

import com.destore.analytics.dto.RevenueReportDTO;
import com.destore.analytics.repository.RevenueAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service layer for revenue analytics business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueAnalyticsService {

    private final RevenueAnalyticsRepository repository;

    /**
     * Generate comprehensive revenue report for a store.
     */
    public RevenueReportDTO generateRevenueReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue report for store {} from {} to {}", storeId, startDate, endDate);

        // Validate date range
        validateDateRange(startDate, endDate);

        // Get store name
        String storeName = repository.getStoreName(storeId);

        // Get revenue statistics
        var stats = repository.getRevenueStats(storeId, startDate, endDate);

        // Get daily breakdown
        var dailyBreakdown = repository.getDailyRevenue(storeId, startDate, endDate);

        // Get payment method breakdown
        var paymentBreakdown = repository.getPaymentMethodBreakdown(storeId, startDate, endDate);

        return RevenueReportDTO.builder()
            .storeId(storeId)
            .storeName(storeName)
            .startDate(startDate)
            .endDate(endDate)
            .totalRevenue(stats.totalRevenue)
            .transactionCount(stats.transactionCount)
            .averageTransactionValue(stats.averageValue)
            .minTransactionValue(stats.minValue)
            .maxTransactionValue(stats.maxValue)
            .dailyBreakdown(dailyBreakdown)
            .paymentMethodBreakdown(paymentBreakdown)
            .build();
    }

    /** 
     * @param startDate
     * @param endDate
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 365) {
            throw new IllegalArgumentException("Date range cannot exceed 365 days");
        }
    }
}
