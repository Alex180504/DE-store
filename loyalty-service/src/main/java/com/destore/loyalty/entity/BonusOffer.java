package com.destore.loyalty.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing bonus offers for spending thresholds.
 * <p>
 * Defines extra loyalty points awarded when customers reach spending milestones
 * (e.g., 20 bonus points for purchases over £50). Supports global and store-specific offers.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Entity
@Table(name = "bonus_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusOffer {

    /**
     * Unique identifier for the bonus offer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bonus_id")
    private Long bonusId;

    /**
     * Store ID where this offer applies (NULL for global offers).
     */
    @Column(name = "store_id")
    private Long storeId;

    /**
     * Name of the bonus offer.
     */
    @Column(name = "offer_name", nullable = false, length = 200)
    private String offerName;

    /**
     * Spending threshold required to trigger the bonus (in GBP).
     */
    @Column(name = "threshold_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdAmount;

    /**
     * Bonus points awarded when threshold is reached.
     */
    @Column(name = "bonus_points", nullable = false, precision = 10, scale = 2)
    private BigDecimal bonusPoints;

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
     * Checks if this offer is applicable to a specific store.
     *
     * @param targetStoreId The store ID to check
     * @return true if offer applies to the store (global or matching store ID)
     */
    public boolean appliesToStore(Long targetStoreId) {
        return storeId == null || storeId.equals(targetStoreId);
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
     * Checks if a transaction total qualifies for this bonus.
     *
     * @param transactionTotal Total purchase amount
     * @return true if the transaction total meets or exceeds the threshold
     */
    public boolean qualifiesForBonus(BigDecimal transactionTotal) {
        return transactionTotal.compareTo(thresholdAmount) >= 0;
    }
}
