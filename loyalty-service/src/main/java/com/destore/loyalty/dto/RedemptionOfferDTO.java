package com.destore.loyalty.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for redemption offer information.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionOfferDTO {

    /**
     * Offer ID.
     */
    private Long redemptionId;

    /**
     * Store ID (null for global).
     */
    private Long storeId;

    /**
     * Item ID (null for category-wide).
     */
    private Long itemId;

    /**
     * Offer name.
     */
    private String offerName;

    /**
     * Offer description.
     */
    private String description;

    /**
     * Discount percentage.
     */
    private BigDecimal discountPercentage;

    /**
     * Points cost.
     */
    private BigDecimal pointsCost;

    /**
     * Maximum uses per customer.
     */
    private Integer maxUsesPerCustomer;

    /**
     * Maximum total uses.
     */
    private Integer maxTotalUses;

    /**
     * Current total uses.
     */
    private Integer currentTotalUses;

    /**
     * Valid from timestamp.
     */
    private LocalDateTime validFrom;

    /**
     * Valid to timestamp.
     */
    private LocalDateTime validTo;

    /**
     * Whether offer is available for use.
     */
    private Boolean available;
}
