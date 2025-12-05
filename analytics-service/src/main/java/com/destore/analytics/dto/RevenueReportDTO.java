package com.destore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Revenue report data transfer object containing aggregated financial metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDTO {
    
    private Long storeId;
    private String storeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private Long transactionCount;
    private BigDecimal averageTransactionValue;
    private BigDecimal minTransactionValue;
    private BigDecimal maxTransactionValue;
    
    /**
     * Daily revenue breakdown for trend analysis
     */
    private List<DailyRevenue> dailyBreakdown;
    
    /**
     * Payment method distribution
     */
    private List<PaymentMethodRevenue> paymentMethodBreakdown;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Long transactionCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodRevenue {
        private String paymentMethod;
        private BigDecimal revenue;
        private Long transactionCount;
        private BigDecimal percentage;
    }
}
