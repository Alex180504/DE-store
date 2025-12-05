package com.destore.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating/updating bonus offers.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusOfferRequest {

    /**
     * Store ID (null for global offers).
     */
    private Integer storeId;

    /**
     * Name of the bonus offer.
     */
    @NotBlank(message = "Offer name is required")
    @Size(max = 200, message = "Offer name must not exceed 200 characters")
    private String offerName;

    /**
     * Minimum spend threshold.
     */
    @NotNull(message = "Threshold amount is required")
    @DecimalMin(value = "0.01", message = "Threshold must be at least 0.01")
    private BigDecimal thresholdAmount;

    /**
     * Bonus points awarded.
     */
    @NotNull(message = "Bonus points is required")
    @Min(value = 1, message = "Bonus points must be at least 1")
    private Integer bonusPoints;

    /**
     * Offer valid from date.
     */
    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    /**
     * Offer valid to date (null for ongoing).
     */
    private LocalDateTime validTo;

    /**
     * Whether the offer is active.
     */
    @Builder.Default
    private Boolean isActive = true;
}
