package com.destore.pricing.model.dto;

import com.destore.pricing.model.enums.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @file PricingRuleResponse.java
 * @brief DTO for returning pricing rule information
 * 
 * Used as response object when listing or retrieving pricing rules.
 * Store information should be fetched from store-service using storeId.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Pricing rule details")
public class PricingRuleResponse {

    @Schema(description = "Unique rule identifier", example = "1")
    private Integer ruleId;

    @Schema(description = "Item ID from warehouse", example = "1")
    private Integer itemId;

    @Schema(description = "Store ID (null = global rule, references store-service.stores.store_id)", example = "2")
    private Integer storeId;

    @Schema(description = "Item price", example = "79.99")
    private BigDecimal price;

    @Schema(description = "Promotion type", example = "PERCENTAGE_OFF")
    private PromotionType promotion;

    @Schema(description = "Promotion value", example = "10.00")
    private BigDecimal promotionValue;

    @Schema(description = "Is this a global rule", example = "false")
    private Boolean isGlobal;

    @Schema(description = "Rule start date/time", example = "2025-12-01T00:00:00")
    private LocalDateTime validFrom;

    @Schema(description = "Rule end date/time", example = "2025-12-31T23:59:59")
    private LocalDateTime validTo;

    @Schema(description = "Is the rule active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creator username", example = "store_manager_lon")
    private String createdBy;

    @Schema(description = "Creation timestamp", example = "2025-12-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-12-01T10:00:00")
    private LocalDateTime updatedAt;
}
