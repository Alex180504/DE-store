package com.destore.loyalty.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing loyalty point redemption offers.
 * <p>
 * Defines discounts customers can redeem by spending loyalty points
 * (e.g., 10% off cordless drills for 50 points). Supports usage limits
 * per customer and total redemptions.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Entity
@Table(name = "redemption_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionOffer {

    /**
     * Unique identifier for the redemption offer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_id")
    private Long redemptionId;

    /**
     * Store ID where this offer applies (NULL for global offers).
     */
    @Column(name = "store_id")
    private Long storeId;

    /**
     * Product/item ID from warehouse catalog (NULL for category-wide offers).
     */
    @Column(name = "item_id")
    private Long itemId;

    /**
     * Name of the redemption offer.
     */
    @Column(name = "offer_name", nullable = false, length = 200)
    private String offerName;

    /**
     * Description of the offer.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Discount percentage awarded when points are redeemed (0-100).
     */
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    /**
     * Loyalty points cost to redeem this offer.
     */
    @Column(name = "points_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal pointsCost;

    /**
     * Maximum times a customer can redeem this offer (NULL for unlimited).
     */
    @Column(name = "max_uses_per_customer")
    private Integer maxUsesPerCustomer;

    /**
     * Maximum total redemptions across all customers (NULL for unlimited).
     */
    @Column(name = "max_total_uses")
    private Integer maxTotalUses;

    /**
     * Current count of total redemptions.
     */
    @Builder.Default
    @Column(name = "current_total_uses", nullable = false)
    private Integer currentTotalUses = 0;

    /**
     * Start date of offer validity (inclusive).
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    /**
     * End date of offer validity (exclusive, NULL for ongoing offers).
     */
    @Column(name = "valid_to")
    private LocalDateTime validTo;

    /**
     * Indicates if this offer is currently active.
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
     * Optimistic locking version.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

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
     * Checks if this offer is applicable to a specific store and item.
     *
     * @param targetStoreId The store ID to check
     * @param targetItemId The item ID to check
     * @return true if offer applies to the store and item
     */
    public boolean appliesToStoreAndItem(Long targetStoreId, Long targetItemId) {
        boolean storeMatches = storeId == null || storeId.equals(targetStoreId);
        boolean itemMatches = itemId == null || itemId.equals(targetItemId);
        return storeMatches && itemMatches;
    }

    /**
     * Checks if this offer is valid at a specific timestamp.
     *
     * @param timestamp The timestamp to check
     * @return true if the offer is valid at the given timestamp
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
     * Checks if total usage limit has been reached.
     *
     * @return true if the offer can still be used globally
     */
    public boolean hasAvailableUses() {
        return maxTotalUses == null || currentTotalUses < maxTotalUses;
    }

    /**
     * Increments the total usage count.
     * <p>
     * Should be called within a transaction with optimistic locking.
     * </p>
     */
    public void incrementUsage() {
        this.currentTotalUses++;
    }
}
