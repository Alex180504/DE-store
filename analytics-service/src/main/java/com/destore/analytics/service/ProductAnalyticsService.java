package com.destore.analytics.service;

import com.destore.analytics.dto.ProductPerformanceDTO;
import com.destore.analytics.repository.ProductAnalyticsRepository;
import com.destore.analytics.repository.RevenueAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service layer for product performance analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAnalyticsService {

    private final ProductAnalyticsRepository repository;
    private final RevenueAnalyticsRepository revenueRepository;

    /**
     * Generate product performance report with top sellers and category analysis.
     */
    public ProductPerformanceDTO generateProductPerformance(Long storeId, LocalDate startDate, LocalDate endDate, Integer topProductsLimit) {
        log.info("Generating product performance for store {} from {} to {}", storeId, startDate, endDate);

        validateDateRange(startDate, endDate);

        if (topProductsLimit == null || topProductsLimit < 1) {
            topProductsLimit = 10;
        } else if (topProductsLimit > 100) {
            topProductsLimit = 100;
        }

        String storeName = revenueRepository.getStoreName(storeId);

        var topProducts = repository.getTopProducts(storeId, startDate, endDate, topProductsLimit);
        var categoryPerformance = repository.getCategoryPerformance(storeId, startDate, endDate);

        return ProductPerformanceDTO.builder()
            .storeId(storeId)
            .storeName(storeName)
            .startDate(startDate)
            .endDate(endDate)
            .topProducts(topProducts)
            .categoryPerformance(categoryPerformance)
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
    }
}
