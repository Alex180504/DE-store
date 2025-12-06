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
public interface ProductPointsRuleRepository extends JpaRepository<ProductPointsRule, Integer> {

    /**
     * Finds all product points rules that are not deleted.
     * Includes both active and inactive rules for UI display.
     *
     * @return List of non-deleted rules
     */
    List<ProductPointsRule> findByIsDeletedFalse();

    /**
     * Finds product points rules valid at a specific timestamp.
     * <p>
     * Used for historical points calculation from past transactions.
     * Does NOT filter by isActive to allow calculation of points earned
     * under rules that are now deactivated/archived.
     * </p>
     *
     * @param itemId Product/item ID
     * @param timestamp Transaction timestamp
     * @return List of applicable rules
     */
    @Query("SELECT r FROM ProductPointsRule r WHERE r.itemId = :itemId " +
           "AND r.validFrom <= :timestamp " +
           "AND (r.validTo IS NULL OR r.validTo > :timestamp)")
    List<ProductPointsRule> findApplicableRules(@Param("itemId") Integer itemId,
                                                 @Param("timestamp") LocalDateTime timestamp);

    /**
     * Finds all rules for a specific item (including inactive for historical reference).
     *
     * @param itemId Product/item ID
     * @return List of all rules for the item
     */
    List<ProductPointsRule> findByItemId(Integer itemId);
}
