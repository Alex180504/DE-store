package com.destore.analytics.service;

import com.destore.analytics.dto.StoreComparisonDTO;
import com.destore.analytics.repository.StoreAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for store comparison and network analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreAnalyticsService {

    private final StoreAnalyticsRepository repository;

    /**
     * Generate store comparison report across all stores in the network.
     */
    public StoreComparisonDTO generateStoreComparison(LocalDate startDate, LocalDate endDate) {
        log.info("Generating store comparison from {} to {}", startDate, endDate);

        validateDateRange(startDate, endDate);

        var allStoreMetrics = repository.getAllStoreMetrics(startDate, endDate);
        var networkTotals = repository.getNetworkTotals(startDate, endDate);

        // Calculate growth rates for each store
        List<StoreComparisonDTO.StoreMetrics> storeMetricsList = allStoreMetrics.stream()
            .map(data -> {
                BigDecimal growthRate = repository.calculateGrowthRate(data.storeId, startDate, endDate);
                
                return StoreComparisonDTO.StoreMetrics.builder()
                    .storeId(data.storeId)
                    .storeName(data.storeName)
                    .location(data.location)
                    .region(data.region)
                    .revenue(data.revenue)
                    .transactionCount(data.transactionCount)
                    .averageTransactionValue(data.avgTransactionValue)
                    .rank(data.rank)
                    .percentageOfNetwork(data.percentageOfNetwork)
                    .growthRate(growthRate)
                    .build();
            })
            .toList();

        BigDecimal avgRevenuePerStore = networkTotals.storeCount > 0
            ? networkTotals.totalRevenue.divide(
                BigDecimal.valueOf(networkTotals.storeCount), 
                2, 
                BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;

        return StoreComparisonDTO.builder()
            .startDate(startDate)
            .endDate(endDate)
            .stores(storeMetricsList)
            .totalNetworkRevenue(networkTotals.totalRevenue)
            .totalNetworkTransactions(networkTotals.totalTransactions)
            .averageRevenuePerStore(avgRevenuePerStore)
            .build();
    }

    /**
     * Get performance metrics for a specific store.
     */
    public StoreComparisonDTO.StoreMetrics getStorePerformance(Long storeId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting performance for store {} from {} to {}", storeId, startDate, endDate);

        validateDateRange(startDate, endDate);

        var data = repository.getStoreMetrics(storeId, startDate, endDate);
        BigDecimal growthRate = repository.calculateGrowthRate(storeId, startDate, endDate);

        return StoreComparisonDTO.StoreMetrics.builder()
            .storeId(data.storeId)
            .storeName(data.storeName)
            .location(data.location)
            .region(data.region)
            .revenue(data.revenue)
            .transactionCount(data.transactionCount)
            .averageTransactionValue(data.avgTransactionValue)
            .growthRate(growthRate)
            .build();
    }

    /**
     * Get all stores for dropdown selection.
     */
    public List<StoreAnalyticsRepository.StoreInfo> getAllStores() {
        log.debug("Fetching all active stores");
        return repository.getAllStores();
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
    }
}
