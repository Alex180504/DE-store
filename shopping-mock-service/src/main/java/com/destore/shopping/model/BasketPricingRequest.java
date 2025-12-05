package com.destore.shopping.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Basket Pricing Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketPricingRequest {
    
    public Integer customerId;
    public Integer storeId;
    public String basketReference;
    public List<BasketItem> items;
    public List<Integer> selectedOfferIds;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasketItem {
        public Integer itemId;
        public String itemName;
        public Integer quantity;
        public BigDecimal unitPrice;
    }
}
