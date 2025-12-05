package com.destore.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating/updating product points rules.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPointsRuleRequest {

    /**
     * Product/item ID.
     */
    @NotNull(message = "Item ID is required")
    private Integer itemId;

    /**
     * Fixed points per unit.
     */
    @Min(value = 0, message = "Points per unit must be non-negative")
    private Integer pointsPerUnit;

    /**
     * Points per £1 spent.
     */
    @DecimalMin(value = "0.01", message = "Points per GBP must be at least 0.01")
    private BigDecimal pointsPerPound;

    /**
     * Rule valid from date.
     */
    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    /**
     * Rule valid to date (null for ongoing).
     */
    private LocalDateTime validTo;

    /**
     * Whether the rule is active.
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether the rule is deleted.
     */
    @Builder.Default
    private Boolean isDeleted = false;
}
