package com.destore.loyalty.repository;

import com.destore.loyalty.entity.ProductPointsRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ProductPointsRule entity operations.
 * <p>
 * Provides data access methods for managing product points rules,
 * including historical queries for retroactive points calculation.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Repository
public interface ProductPointsRuleRepository extends JpaRepository<ProductPointsRule, Long> {

    /**
     * Finds all active product points rules.
     *
     * @return List of active rules
     */
    List<ProductPointsRule> findByIsActiveTrue();

    /**
     * Finds product points rules valid at a specific timestamp.
     * <p>
     * Used for historical points calculation from past transactions.
     * </p>
     *
     * @param itemId Product/item ID
     * @param storeId Store ID (null for global rules)
     * @param timestamp Transaction timestamp
     * @return List of applicable rules (global rules included)
     */
    @Query("SELECT r FROM ProductPointsRule r WHERE r.itemId = :itemId " +
           "AND (r.storeId IS NULL OR r.storeId = :storeId) " +
           "AND r.isActive = true " +
           "AND r.validFrom <= :timestamp " +
           "AND (r.validTo IS NULL OR r.validTo > :timestamp) " +
           "ORDER BY r.storeId DESC NULLS LAST")
    List<ProductPointsRule> findApplicableRules(@Param("itemId") Long itemId,
                                                 @Param("storeId") Long storeId,
                                                 @Param("timestamp") LocalDateTime timestamp);

    /**
     * Finds all rules for a specific item (including inactive for historical reference).
     *
     * @param itemId Product/item ID
     * @return List of all rules for the item
     */
    List<ProductPointsRule> findByItemId(Long itemId);

    /**
     * Finds active rules for a specific store.
     *
     * @param storeId Store ID
     * @return List of store-specific active rules
     */
    List<ProductPointsRule> findByStoreIdAndIsActiveTrue(Long storeId);
}
