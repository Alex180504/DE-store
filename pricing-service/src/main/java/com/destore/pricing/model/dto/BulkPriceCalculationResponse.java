package com.destore.pricing.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @file BulkPriceCalculationResponse.java
 * @brief DTO for bulk price calculation response
 * 
 * Contains pricing information for multiple items with totals.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing prices for multiple items")
public class BulkPriceCalculationResponse {

    @Schema(description = "Store ID used for pricing")
    private Integer storeId;

    @Schema(description = "Individual item prices")
    private List<ItemPriceDetails> itemPrices;

    @Schema(description = "Total subtotal (sum of all item subtotals)")
    private BigDecimal totalSubtotal;

    @Schema(description = "Total discount amount (sum of all discounts)")
    private BigDecimal totalDiscount;

    @Schema(description = "Final total price (totalSubtotal - totalDiscount)")
    private BigDecimal finalTotal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Price details for an individual item")
    public static class ItemPriceDetails {
        
        @Schema(description = "Item ID")
        private Integer itemId;
        
        @Schema(description = "Quantity")
        private Integer quantity;
        
        @Schema(description = "Unit price")
        private BigDecimal unitPrice;
        
        @Schema(description = "Subtotal (unitPrice × quantity)")
        private BigDecimal subtotal;
        
        @Schema(description = "Discount amount")
        private BigDecimal discount;
        
        @Schema(description = "Final price after discount")
        private BigDecimal finalPrice;
        
        @Schema(description = "Promotion type applied")
        private String promotion;
    }
}
