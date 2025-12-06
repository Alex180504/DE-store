package com.destore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Store comparison and benchmarking metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreComparisonDTO {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private List<StoreMetrics> stores;
    
    // Network-wide aggregates
    private BigDecimal totalNetworkRevenue;
    private Long totalNetworkTransactions;
    private BigDecimal averageRevenuePerStore;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreMetrics {
        private Long storeId;
        private String storeName;
        private String location;
        private String region;
        
        private BigDecimal revenue;
        private Long transactionCount;
        private BigDecimal averageTransactionValue;
        
        private Integer rank; // Performance ranking
        private BigDecimal percentageOfNetwork; // % of total network revenue
        private BigDecimal growthRate; // Compared to previous period
    }
}
