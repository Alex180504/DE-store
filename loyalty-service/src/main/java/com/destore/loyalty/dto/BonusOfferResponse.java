package com.destore.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for bonus offer responses.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusOfferResponse {

    private Integer offerId;
    private Integer storeId;
    private String offerName;
    private BigDecimal thresholdAmount;
    private Integer bonusPoints;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
