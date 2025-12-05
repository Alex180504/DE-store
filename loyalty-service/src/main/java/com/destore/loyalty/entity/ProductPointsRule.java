package com.destore.loyalty.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entity representing loyalty points rules for products.
 * <p>
 * Defines how many points customers earn when purchasing specific products.
 * Supports both per-unit points and percentage-based points (points per £1 spent).
 * Rules have validity periods to maintain historical accuracy for retroactive calculations.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Entity
@Table(name = "product_points_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPointsRule {

    /**
     * Unique identifier for the product points rule.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer ruleId;

    /**
     * Product/item ID from warehouse catalog.
     */
    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    /**
     * Fixed points awarded per unit purchased.
     * <p>
     * Used for products with fixed point values (e.g., 5 points per drill).
     * Mutually exclusive with pointsPerPound.
     * </p>
     */
    @Column(name = "points_per_unit", precision = 10, scale = 2)
    private BigDecimal pointsPerUnit;

    /**
     * Points awarded per £1 spent on this product.
     * <p>
     * Used for percentage-based rules (e.g., 0.5 points per £1 on paint).
     * Mutually exclusive with pointsPerUnit.
     * </p>
     */
    @Column(name = "points_per_gbp", precision = 10, scale = 2)
    private BigDecimal pointsPerPound;

    /**
     * Start date of rule validity (inclusive).
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    /**
     * End date of rule validity (exclusive, NULL for ongoing rules).
     */
    @Column(name = "valid_to")
    private LocalDateTime validTo;

    /**
     * Indicates if this rule is currently active.
     * <p>
     * Soft delete flag - rules are never hard deleted to preserve historical accuracy.
     * </p>
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Record creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets creation timestamp before persisting new entity.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Updates modification timestamp before updating entity.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this rule is valid at a specific timestamp.
     *
     * @param timestamp The timestamp to check
     * @return true if the rule is valid at the given timestamp
     */
    public boolean isValidAt(LocalDateTime timestamp) {
        if (!isActive) {
            return false;
        }
        if (timestamp.isBefore(validFrom)) {
            return false;
        }
        return validTo == null || timestamp.isBefore(validTo);
    }

    /**
     * Calculates points earned for a purchase.
     *
     * @param quantity Quantity of items purchased
     * @param totalPrice Total price paid for the items
     * @return Calculated loyalty points (rounded down to 2 decimal places)
     */
    public BigDecimal calculatePoints(BigDecimal quantity, BigDecimal totalPrice) {
        if (pointsPerUnit != null) {
            return pointsPerUnit.multiply(quantity).setScale(2, RoundingMode.DOWN);
        } else if (pointsPerPound != null) {
            return pointsPerPound.multiply(totalPrice).setScale(2, RoundingMode.DOWN);
        }
        return BigDecimal.ZERO;
    }
}
