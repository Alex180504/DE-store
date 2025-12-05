package com.destore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Product performance metrics and top sellers analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPerformanceDTO {
    
    private Long storeId;
    private String storeName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private List<TopProduct> topProducts;
    private List<CategoryPerformance> categoryPerformance;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long itemId;
        private String itemName;
        private String category;
        private Integer quantitySold;
        private BigDecimal totalRevenue;
        private BigDecimal averagePrice;
        private Long transactionCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        private String category;
        private Integer totalQuantity;
        private BigDecimal totalRevenue;
        private Long uniqueProducts;
        private BigDecimal averagePrice;
        private BigDecimal percentageOfTotal;
    }
}
