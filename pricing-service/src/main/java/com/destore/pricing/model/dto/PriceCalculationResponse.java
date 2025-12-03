package com.destore.pricing.model.dto;

import com.destore.pricing.model.enums.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @file PriceCalculationResponse.java
 * @brief DTO for returning calculated price information
 * 
 * Provides detailed breakdown of price calculation including base price,
 * promotion discounts, and final price. Used by Shopping System to
 * display pricing information to customers.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Calculated price details with promotion breakdown")
public class PriceCalculationResponse {

    @Schema(description = "Item ID", example = "1")
    private Integer itemId;

    @Schema(description = "Item name from warehouse", example = "Cordless Drill")
    private String itemName;

    @Schema(description = "Store ID used for calculation", example = "2")
    private Integer storeId;

    @Schema(description = "Quantity purchased", example = "3")
    private Integer quantity;

    @Schema(description = "Unit price before promotions", example = "79.99")
    private BigDecimal unitPrice;

    @Schema(description = "Subtotal (unitPrice * quantity)", example = "239.97")
    private BigDecimal subtotal;

    @Schema(description = "Promotion type applied", example = "PERCENTAGE_OFF")
    private PromotionType promotionApplied;

    @Schema(description = "Promotion value (percentage or fixed amount)", example = "10")
    private BigDecimal promotionValue;

    @Schema(description = "Discount amount calculated", example = "23.99")
    private BigDecimal discount;

    @Schema(description = "Final price after promotions", example = "215.98")
    private BigDecimal finalPrice;

    @Schema(description = "Rule source: GLOBAL or STORE_SPECIFIC", example = "STORE_SPECIFIC")
    private String ruleSource;

    @Schema(description = "Rule ID that was applied", example = "5")
    private Integer appliedRuleId;
}
