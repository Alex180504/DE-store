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

    @Schema(description = "Store ID used for calculation", example = "2")
    private Long storeId;

    @Schema(description = "Quantity purchased", example = "3")
    private Integer quantity;

    @Schema(description = "Unit price before promotions", example = "79.99")
    private BigDecimal unitPrice;

    @Schema(description = "Subtotal (unitPrice * quantity)", example = "239.97")
    private BigDecimal subtotal;

    @Schema(description = "Promotion applied", example = "PERCENTAGE_OFF")
    private PromotionType promotion;

    @Schema(description = "Promotion discount amount", example = "23.99")
    private BigDecimal discountAmount;

    @Schema(description = "Final price after promotions", example = "215.98")
    private BigDecimal finalPrice;

    @Schema(description = "Whether a store-specific rule was used", example = "true")
    private Boolean usedStoreSpecificRule;

    @Schema(description = "Rule ID that was applied", example = "5")
    private Long appliedRuleId;

    @Schema(description = "Calculation details/notes", example = "10% discount applied")
    private String calculationNotes;
}
