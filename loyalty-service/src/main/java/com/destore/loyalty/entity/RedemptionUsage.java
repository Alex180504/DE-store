package com.destore.loyalty.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Entity tracking customer usage of redemption offers.
 * <p>
 * Enforces per-customer usage limits for redemption offers.
 * Used to prevent customers from exceeding max_uses_per_customer constraints.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Entity
@Table(name = "redemption_usage", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "redemption_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionUsage {

    /**
     * Unique identifier for the redemption usage record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Integer usageId;

    /**
     * Customer ID from accounting database.
     */
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    /**
     * Reference to redemption offer.
     */
    @Column(name = "redemption_offer_id", nullable = false)
    private Integer redemptionId;

    /**
     * Number of times customer has used this offer.
     */
    @Builder.Default
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    /**
     * Timestamp of first usage.
     */
    @Column(name = "first_used_at")
    private LocalDateTime firstUsedAt;

    /**
     * Timestamp of most recent usage.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

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
     * Increments usage count and updates timestamps.
     */
    public void incrementUsage() {
        this.usageCount++;
        LocalDateTime now = LocalDateTime.now();
        if (this.firstUsedAt == null) {
            this.firstUsedAt = now;
        }
        this.lastUsedAt = now;
    }

    /**
     * Checks if customer has reached usage limit for an offer.
     *
     * @param maxUsesPerCustomer Maximum allowed uses (null for unlimited)
     * @return true if customer can still use the offer
     */
    public boolean canUse(Integer maxUsesPerCustomer) {
        return maxUsesPerCustomer == null || this.usageCount < maxUsesPerCustomer;
    }
}
