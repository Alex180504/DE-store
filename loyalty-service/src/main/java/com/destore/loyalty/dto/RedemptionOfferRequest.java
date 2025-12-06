package com.destore.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating/updating redemption offers.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionOfferRequest {

    /**
     * Item ID (null for any item).
     */
    private Integer itemId;

    /**
     * Name of the redemption offer.
     */
    @NotBlank(message = "Offer name is required")
    @Size(max = 200, message = "Offer name must not exceed 200 characters")
    private String offerName;

    /**
     * Description of the offer.
     */
    private String description;

    /**
     * Discount percentage.
     */
    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.01", message = "Discount must be at least 0.01%")
    @DecimalMax(value = "100.00", message = "Discount cannot exceed 100%")
    private BigDecimal discountPercentage;

    /**
     * Points cost.
     */
    @NotNull(message = "Points cost is required")
    @DecimalMin(value = "0.01", message = "Points cost must be at least 0.01")
    private BigDecimal pointsCost;

    /**
     * Max uses per customer (null for unlimited).
     */
    @Min(value = 1, message = "Max uses per customer must be at least 1")
    private Integer maxUsesPerCustomer;

    /**
     * Max total uses (null for unlimited).
     */
    @Min(value = 1, message = "Max total uses must be at least 1")
    private Integer maxTotalUses;

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
