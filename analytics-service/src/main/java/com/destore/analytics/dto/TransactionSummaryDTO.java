package com.destore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Transaction summary containing volume and status metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDTO {
    
    private Long storeId;
    private String storeName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Overall metrics
    private Long totalTransactions;
    private BigDecimal totalVolume;
    private BigDecimal averageValue;
    
    // Status breakdown
    private Long completedCount;
    private Long pendingCount;
    private Long cancelledCount;
    private Long refundedCount;
    
    private BigDecimal completedVolume;
    private BigDecimal pendingVolume;
    private BigDecimal cancelledVolume;
    private BigDecimal refundedVolume;
    
    /**
     * Hourly transaction distribution
     */
    private List<HourlyDistribution> hourlyDistribution;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyDistribution {
        private Integer hour; // 0-23
        private Long transactionCount;
        private BigDecimal revenue;
    }
}
