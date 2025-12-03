package com.destore.pricing.model.dto;

import com.destore.pricing.model.enums.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @file PricingRuleRequest.java
 * @brief DTO for creating or updating pricing rules
 * 
 * Used by Store Managers and Network Managers to create pricing rules.
 * Includes validation constraints to ensure data integrity.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create or update a pricing rule")
public class PricingRuleRequest {

    @NotNull(message = "Item ID is required")
    @Positive(message = "Item ID must be positive")
    @Schema(description = "ID of the item from warehouse database", example = "1")
    private Integer itemId;

    @Schema(description = "Store ID for store-specific pricing (null = global)", example = "2")
    private Long storeId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have max 8 digits and 2 decimals")
    @Schema(description = "Item price", example = "79.99")
    private BigDecimal price;

    @NotNull(message = "Promotion type is required")
    @Schema(description = "Type of promotion", example = "PERCENTAGE_OFF")
    private PromotionType promotion;

    @DecimalMin(value = "0.0", inclusive = true, message = "Promotion value must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Promotion value must have max 8 digits and 2 decimals")
    @Schema(description = "Promotion value (for PERCENTAGE_OFF or FIXED_DISCOUNT)", example = "10.00")
    private BigDecimal promotionValue;

    @NotNull(message = "is_global flag is required")
    @Schema(description = "Whether this is a global (network-wide) rule", example = "false")
    private Boolean isGlobal;

    @Schema(description = "Start date/time for rule validity", example = "2025-12-01T00:00:00")
    private LocalDateTime validFrom;

    @Schema(description = "End date/time for rule validity (null = no expiration)", example = "2025-12-31T23:59:59")
    private LocalDateTime validTo;

    @Schema(description = "Username of manager creating the rule", example = "store_manager_lon")
    private String createdBy;

    /**
     * @brief Validate that promotion value is provided when needed
     * @return true if validation passes
     */
    @AssertTrue(message = "Promotion value required for PERCENTAGE_OFF and FIXED_DISCOUNT")
    public boolean isPromotionValueValid() {
        if (promotion == PromotionType.PERCENTAGE_OFF || promotion == PromotionType.FIXED_DISCOUNT) {
            return promotionValue != null && promotionValue.compareTo(BigDecimal.ZERO) > 0;
        }
        return true;
    }

    /**
     * @brief Validate that global rules don't have a store_id
     * @return true if validation passes
     */
    @AssertTrue(message = "Global rules cannot have a store_id")
    public boolean isGlobalStoreConsistent() {
        if (Boolean.TRUE.equals(isGlobal)) {
            return storeId == null;
        }
        return true;
    }

    /**
     * @brief Validate date range
     * @return true if validation passes
     */
    @AssertTrue(message = "valid_to must be after valid_from")
    public boolean isDateRangeValid() {
        if (validFrom != null && validTo != null) {
            return validTo.isAfter(validFrom);
        }
        return true;
    }
}
