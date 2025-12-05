package com.destore.loyalty.repository;

import com.destore.loyalty.entity.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for PointsTransaction entity operations.
 * <p>
 * Provides data access methods for points transaction audit trail.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {

    /**
     * Finds all transactions for a specific customer.
     *
     * @param customerId Customer ID
     * @return List of transactions ordered by creation date descending
     */
    List<PointsTransaction> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    /**
     * Finds transactions for a customer within a date range.
     *
     * @param customerId Customer ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @return List of transactions in the date range
     */
    List<PointsTransaction> findByCustomerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Integer customerId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds transactions by type for a customer.
     *
     * @param customerId Customer ID
     * @param transactionType Type of transaction
     * @return List of matching transactions
     */
    List<PointsTransaction> findByCustomerIdAndTransactionType(
            Integer customerId, PointsTransaction.TransactionType transactionType);

    /**
     * Finds transactions associated with a specific basket reference.
     * <p>
     * Used for SAGA pattern compensation and audit tracking.
     * </p>
     *
     * @param basketReference Basket reference UUID
     * @return List of transactions for the basket
     */
    List<PointsTransaction> findByBasketReference(String basketReference);

    /**
     * Calculates total points earned by a customer.
     *
     * @param customerId Customer ID
     * @return Total earned points (or 0 if no transactions)
     */
    @Query("SELECT COALESCE(SUM(pt.pointsAmount), 0) FROM PointsTransaction pt " +
           "WHERE pt.customerId = :customerId " +
           "AND pt.transactionType IN ('EARNED', 'BONUS', 'ADJUSTMENT')")
    Integer calculateTotalEarned(@Param("customerId") Integer customerId);

    /**
     * Calculates total points redeemed by a customer.
     *
     * @param customerId Customer ID
     * @return Total redeemed points (or 0 if no transactions)
     */
    @Query("SELECT COALESCE(SUM(ABS(pt.pointsAmount)), 0) FROM PointsTransaction pt " +
           "WHERE pt.customerId = :customerId " +
           "AND pt.transactionType = 'REDEEMED'")
    Integer calculateTotalRedeemed(@Param("customerId") Integer customerId);
}
