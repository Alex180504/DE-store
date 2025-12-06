package com.destore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Customer behavior and segmentation analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsDTO {
    
    private Long storeId;
    private String storeName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Overall customer metrics
    private Long totalCustomers;
    private Long activeCustomers; // Made at least one transaction
    private Long newCustomers; // First transaction in period
    private Long returningCustomers;
    
    private BigDecimal averageCustomerValue;
    private BigDecimal customerLifetimeValue;
    
    /**
     * Top customers by spending
     */
    private List<TopCustomer> topCustomers;
    
    /**
     * Customer segmentation by transaction frequency
     */
    private List<CustomerSegment> segments;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private Long customerId;
        private BigDecimal totalSpent;
        private Long transactionCount;
        private BigDecimal averageTransactionValue;
        private LocalDate firstPurchase;
        private LocalDate lastPurchase;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSegment {
        private String segmentName; // e.g., "1-2 purchases", "3-5 purchases", "6+ purchases"
        private Long customerCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageValue;
        private BigDecimal percentageOfCustomers;
        private BigDecimal percentageOfRevenue;
    }
}
