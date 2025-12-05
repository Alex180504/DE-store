package com.destore.loyalty.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a customer's loyalty points balance.
 * <p>
 * Stores current balance, lifetime statistics, and last calculation timestamp.
 * Uses optimistic locking to prevent concurrent modification issues during
 * point redemption.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Entity
@Table(name = "customer_points_balance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPointsBalance {

    /**
     * Unique identifier for the customer points balance record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Integer balanceId;

    /**
     * Customer ID from accounting database.
     */
    @Column(name = "customer_id", nullable = false, unique = true)
    private Integer customerId;

    /**
     * Current available loyalty points balance.
     */
    @Builder.Default
    @Column(name = "current_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    /**
     * Total loyalty points earned over customer lifetime.
     */
    @Builder.Default
    @Column(name = "lifetime_earned", nullable = false, precision = 10, scale = 2)
    private BigDecimal lifetimeEarned = BigDecimal.ZERO;

    /**
     * Total loyalty points redeemed over customer lifetime.
     */
    @Builder.Default
    @Column(name = "lifetime_redeemed", nullable = false, precision = 10, scale = 2)
    private BigDecimal lifetimeRedeemed = BigDecimal.ZERO;

    /**
     * Timestamp of last points calculation from transactions.
     * <p>
     * Used for incremental calculation to avoid reprocessing all transactions.
     * </p>
     */
    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

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
     * <p>
     * Critical for preventing double-spending during concurrent redemptions.
     * </p>
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
     * Credits points to the customer's balance.
     *
     * @param points Points to add (must be positive)
     * @throws IllegalArgumentException if points is negative
     */
    public void creditPoints(BigDecimal points) {
        if (points.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot credit negative points");
        }
        this.currentBalance = this.currentBalance.add(points);
        this.lifetimeEarned = this.lifetimeEarned.add(points);
    }

    /**
     * Debits points from the customer's balance.
     *
     * @param points Points to deduct (must be positive)
     * @throws IllegalArgumentException if points is negative or exceeds balance
     */
    public void debitPoints(BigDecimal points) {
        if (points.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot debit negative points");
        }
        if (points.compareTo(this.currentBalance) > 0) {
            throw new IllegalArgumentException("Insufficient points balance");
        }
        this.currentBalance = this.currentBalance.subtract(points);
        this.lifetimeRedeemed = this.lifetimeRedeemed.add(points);
    }

    /**
     * Checks if customer has sufficient points for a transaction.
     *
     * @param requiredPoints Points needed
     * @return true if balance is sufficient
     */
    public boolean hasSufficientPoints(BigDecimal requiredPoints) {
        return this.currentBalance.compareTo(requiredPoints) >= 0;
    }
}
