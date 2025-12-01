package com.destore.pricing.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @file PriceCalculationRequest.java
 * @brief DTO for requesting price calculation
 * 
 * Used by the Shopping System to calculate final prices with promotions.
 * The calculation engine will apply hierarchical rules (store overrides global)
 * and calculate promotional discounts.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to calculate final price for an item")
public class PriceCalculationRequest {

    @NotNull(message = "Item ID is required")
    @Positive(message = "Item ID must be positive")
    @Schema(description = "ID of the item from warehouse", example = "1", required = true)
    private Integer itemId;

    @Schema(description = "Store ID (null = use global pricing)", example = "2")
    private Long storeId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity being purchased", example = "3", required = true)
    private Integer quantity;
}
