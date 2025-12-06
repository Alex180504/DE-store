package com.destore.loyalty.repository;

import com.destore.loyalty.entity.CustomerPointsBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for CustomerPointsBalance entity operations.
 * <p>
 * Provides data access methods with optimistic locking support
 * to prevent concurrent modification issues during redemption.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Repository
public interface CustomerPointsBalanceRepository extends JpaRepository<CustomerPointsBalance, Integer> {

    /**
     * Finds customer points balance by customer ID.
     *
     * @param customerId Customer ID
     * @return Optional containing balance if found
     */
    Optional<CustomerPointsBalance> findByCustomerId(Integer customerId);

    /**
     * Finds customer points balance with pessimistic write lock.
     * <p>
     * Used during SAGA pattern to reserve points atomically.
     * Prevents concurrent transactions from modifying the same balance.
     * </p>
     *
     * @param customerId Customer ID
     * @return Optional containing locked balance
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cpb FROM CustomerPointsBalance cpb WHERE cpb.customerId = :customerId")
    Optional<CustomerPointsBalance> findByCustomerIdForUpdate(@Param("customerId") Integer customerId);

    /**
     * Checks if a customer balance record exists.
     *
     * @param customerId Customer ID
     * @return true if balance exists
     */
    boolean existsByCustomerId(Integer customerId);
}
