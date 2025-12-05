package com.destore.loyalty.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for basket pricing request.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketPricingRequest {

    /**
     * Customer ID.
     */
    private Integer customerId;

    /**
     * Store ID.
     */
    private Integer storeId;

    /**
     * Basket reference for idempotency.
     */
    private String basketReference;

    /**
     * List of items in the basket.
     */
    private List<BasketItemRequest> items;

    /**
     * Selected redemption offer IDs.
     */
    private List<Integer> selectedOfferIds;

    /**
     * DTO for individual basket item.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasketItemRequest {
        
        /**
         * Item ID from warehouse.
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
         * Base unit price.
         */
        private BigDecimal unitPrice;
    }
}
