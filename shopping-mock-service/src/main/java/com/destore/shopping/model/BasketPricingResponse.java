package com.destore.shopping.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Basket Pricing Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketPricingResponse {
    
    public Integer totalPointsDeducted;
    public List<ItemPrice> itemPrices;
    public String basketReference;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPrice {
        public Integer itemId;
        public Integer quantity;
        public BigDecimal priceBeforeDiscount;
        public BigDecimal priceAfterDiscount;
        public BigDecimal discountApplied;
        public Integer appliedOfferId;
    }
}
