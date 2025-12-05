package com.destore.loyalty.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for basket pricing response.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketPricingResponse {

    /**
     * Basket reference for tracking.
     */
    private String basketReference;

    /**
     * Total points deducted.
     */
    private BigDecimal totalPointsDeducted;

    /**
     * Total discount amount applied.
     */
    private BigDecimal totalDiscountAmount;

    /**
     * Remaining customer points balance.
     */
    private BigDecimal remainingBalance;

    /**
     * List of priced items.
     */
    private List<BasketItemResponse> items;

    /**
     * DTO for individual priced item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasketItemResponse {
        
        /**
         * Item ID.
         */
        private Integer itemId;

        /**
         * Item name.
         */
        private String itemName;

        /**
         * Quantity.
         */
        private Integer quantity;

        /**
         * Original unit price.
         */
        private BigDecimal originalUnitPrice;

        /**
         * Discounted unit price.
         */
        private BigDecimal discountedUnitPrice;

        /**
         * Total line discount.
         */
        private BigDecimal lineDiscount;

        /**
         * Offer ID applied.
         */
        private Integer appliedOfferId;

        /**
         * Offer name applied.
         */
        private String appliedOfferName;
    }
}
