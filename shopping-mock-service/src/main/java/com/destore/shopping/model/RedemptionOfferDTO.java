package com.destore.shopping.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redemption Offer DTO
 * <p>
 * Represents a loyalty redemption offer available for use.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionOfferDTO {
    
    private Integer redemptionId;
    private Integer itemId;
    private String offerName;
    private String description;
    private BigDecimal discountPercentage;
    private Integer pointsCost;
    private Integer maxUsesPerCustomer;
    private Integer maxTotalUses;
    private Integer currentTotalUses;
    private Boolean available;
}
