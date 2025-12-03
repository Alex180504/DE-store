package com.destore.pricing.model.entity;

import com.destore.pricing.model.enums.PromotionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @file PricingRule.java
 * @brief JPA Entity representing a pricing rule
 * 
 * Pricing rules support hierarchical pricing with two levels:
 * - Global (is_global=true, store_id=null): Network-wide pricing set by Network Manager
 * - Store-specific (is_global=false, store_id!=null): Per-store pricing set by Store Manager
 * 
 * Store-specific rules override global rules when calculating final prices.
 * Rules can include promotions (3FOR2, BOGOF, etc.) and time-based validity.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "pricing_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule {

    /**
     * @brief Unique rule identifier (auto-generated)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer ruleId;

    /**
     * @brief Reference to item in warehouse database
     */
    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    /**
     * @brief Store ID for which this rule applies (null = global rule)
     * References store-service.stores.store_id
     */
    @Column(name = "store_id")
    private Integer storeId;

    /**
     * @brief Price for this item (may override base price from warehouse)
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * @brief Type of promotion applied to this item
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion", nullable = false)
    private PromotionType promotion = PromotionType.NONE;

    /**
     * @brief Value for percentage/fixed discount promotions
     * For PERCENTAGE_OFF: 10.00 means 10% off
     * For FIXED_DISCOUNT: 5.00 means £5 off
     */
    @Column(name = "promotion_value", precision = 10, scale = 2)
    private BigDecimal promotionValue;

    /**
     * @brief Whether this is a global (network-wide) rule
     * true = Global rule (managed by Network Manager)
     * false = Store-specific rule (managed by Store Manager)
     */
    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal = false;

    /**
     * @brief Start date/time when this rule becomes active
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom = LocalDateTime.now();

    /**
     * @brief End date/time when this rule expires (null = no expiration)
     */
    @Column(name = "valid_to")
    private LocalDateTime validTo;

    /**
     * @brief Whether this rule is currently active
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * @brief Username of manager who created this rule
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * @brief Timestamp when the rule was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * @brief Timestamp when the rule was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * @brief Check if this rule is currently valid based on date range
     * @return true if current time is within valid_from and valid_to range
     */
    @Transient
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = !now.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !now.isAfter(validTo);
        return isActive && afterStart && beforeEnd;
    }
}
