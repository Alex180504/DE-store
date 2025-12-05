package com.destore.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for product points rule responses.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPointsRuleResponse {

    private Integer ruleId;
    private Integer itemId;
    private Integer pointsPerUnit;
    private BigDecimal pointsPerPound;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deactivatedAt;
}
