package com.destore.pricing.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @file BulkPriceCalculationRequest.java
 * @brief DTO for requesting bulk price calculation
 * 
 * Used by the Shopping System to calculate prices for multiple items
 * in a single request (e.g., during checkout).
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to calculate prices for multiple items")
public class BulkPriceCalculationRequest {

    @NotNull(message = "Store ID is required")
    @Schema(description = "Store ID where items are being purchased", example = "2", required = true)
    private Integer storeId;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    @Schema(description = "List of items to price", required = true)
    private List<BulkPriceItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Individual item in bulk pricing request")
    public static class BulkPriceItem {
        
        @NotNull(message = "Item ID is required")
        @Schema(description = "Item ID from warehouse", example = "1", required = true)
        private Integer itemId;
        
        @NotNull(message = "Quantity is required")
        @Schema(description = "Quantity being purchased", example = "2", required = true)
        private Integer quantity;
    }
}
