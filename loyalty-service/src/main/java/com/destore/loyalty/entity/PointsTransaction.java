package com.destore.loyalty.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing loyalty points transactions.
 * <p>
 * Provides complete audit trail of all points movements including
 * earning, redemption, bonuses, adjustments, expiration, and reversals.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Entity
@Table(name = "points_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsTransaction {

    /**
     * Unique identifier for the points transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    /**
     * Customer ID from accounting database.
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Type of points transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    /**
     * Points amount (positive for credits, negative for debits).
     */
    @Column(name = "points_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal pointsAmount;

    /**
     * Reference to source transaction in accounting database.
     */
    @Column(name = "source_transaction_id")
    private Long sourceTransactionId;

    /**
     * Reference to redemption offer if applicable.
     */
    @Column(name = "redemption_id")
    private Long redemptionId;

    /**
     * Reference to bonus offer if applicable.
     */
    @Column(name = "bonus_id")
    private Long bonusId;

    /**
     * Store ID where transaction occurred.
     */
    @Column(name = "store_id")
    private Long storeId;

    /**
     * Description or notes about the transaction.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Basket reference for redemption transactions (SAGA correlation ID).
     */
    @Column(name = "basket_reference", length = 100)
    private String basketReference;

    /**
     * Transaction creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Sets creation timestamp before persisting new entity.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Enumeration of points transaction types.
     */
    public enum TransactionType {
        /**
         * Points earned from purchases.
         */
        EARNED,

        /**
         * Points redeemed for discounts.
         */
        REDEEMED,

        /**
         * Bonus points from spending thresholds.
         */
        BONUS,

        /**
         * Manual adjustment by admin.
         */
        ADJUSTMENT,

        /**
         * Points expired due to time limit.
         */
        EXPIRED,

        /**
         * Reversal of previous transaction.
         */
        REVERSED
    }
}
