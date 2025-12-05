package com.destore.analytics.service;

import com.destore.analytics.dto.TransactionSummaryDTO;
import com.destore.analytics.repository.RevenueAnalyticsRepository;
import com.destore.analytics.repository.TransactionAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service layer for transaction analytics business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalyticsService {

    private final TransactionAnalyticsRepository repository;
    private final RevenueAnalyticsRepository revenueRepository;

    /**
     * Generate comprehensive transaction summary.
     */
    public TransactionSummaryDTO generateTransactionSummary(Long storeId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating transaction summary for store {} from {} to {}", storeId, startDate, endDate);

        validateDateRange(startDate, endDate);

        String storeName = revenueRepository.getStoreName(storeId);

        var statusBreakdown = repository.getStatusBreakdown(storeId, startDate, endDate);
        var hourlyDistribution = repository.getHourlyDistribution(storeId, startDate, endDate);

        return TransactionSummaryDTO.builder()
            .storeId(storeId)
            .storeName(storeName)
            .startDate(startDate)
            .endDate(endDate)
            .totalTransactions(statusBreakdown.totalTransactions)
            .totalVolume(statusBreakdown.totalVolume)
            .averageValue(statusBreakdown.averageValue)
            .completedCount(statusBreakdown.completedCount)
            .pendingCount(statusBreakdown.pendingCount)
            .cancelledCount(statusBreakdown.cancelledCount)
            .refundedCount(statusBreakdown.refundedCount)
            .completedVolume(statusBreakdown.completedVolume)
            .pendingVolume(statusBreakdown.pendingVolume)
            .cancelledVolume(statusBreakdown.cancelledVolume)
            .refundedVolume(statusBreakdown.refundedVolume)
            .hourlyDistribution(hourlyDistribution)
            .build();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
    }
}
