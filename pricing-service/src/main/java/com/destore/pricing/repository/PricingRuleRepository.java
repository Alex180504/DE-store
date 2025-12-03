package com.destore.pricing.repository;

import com.destore.pricing.model.entity.PricingRule;
import com.destore.pricing.model.enums.PromotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @file PricingRuleRepository.java
 * @brief Spring Data JPA repository for PricingRule entity
 * 
 * Provides CRUD operations and custom queries for pricing rule management.
 * Includes specialized queries for hierarchical pricing (global vs store-specific).
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    /**
     * @brief Find all active pricing rules for a specific item and store
     * 
     * Retrieves rules that are currently active and within their validity period.
     * This query is used by the price calculation engine to determine applicable rules.
     * 
     * @param itemId The warehouse item ID
     * @param storeId The store ID (can be null for global rules)
     * @param now Current timestamp for validity check
     * @return List of active pricing rules (should typically be 0 or 1)
     */
    @Query("SELECT pr FROM PricingRule pr " +
           "WHERE pr.itemId = :itemId " +
           "AND (pr.store.storeId = :storeId OR (pr.isGlobal = true AND pr.store IS NULL)) " +
           "AND pr.isActive = true " +
           "AND pr.validFrom <= :now " +
           "AND (pr.validTo IS NULL OR pr.validTo > :now) " +
           "ORDER BY pr.isGlobal ASC, pr.createdAt DESC")
    List<PricingRule> findActiveRulesForItemAndStore(
            @Param("itemId") Integer itemId,
            @Param("storeId") Long storeId,
            @Param("now") LocalDateTime now
    );

    /**
     * @brief Find active global pricing rule for an item
     * 
     * Retrieves the global (network-wide) pricing rule for an item.
     * Used as fallback when no store-specific rule exists.
     * 
     * @param itemId The warehouse item ID
     * @param now Current timestamp for validity check
     * @return Optional containing the global rule if found
     */
    @Query("SELECT pr FROM PricingRule pr " +
           "WHERE pr.itemId = :itemId " +
           "AND pr.isGlobal = true " +
           "AND pr.store IS NULL " +
           "AND pr.isActive = true " +
           "AND pr.validFrom <= :now " +
           "AND (pr.validTo IS NULL OR pr.validTo > :now)")
    Optional<PricingRule> findActiveGlobalRuleForItem(
            @Param("itemId") Integer itemId,
            @Param("now") LocalDateTime now
    );

    /**
     * @brief Find all pricing rules for a specific item
     * @param itemId The warehouse item ID
     * @return List of all rules (active and inactive) for the item
     */
    List<PricingRule> findByItemId(Integer itemId);

    /**
     * @brief Find all pricing rules for a specific store
     * @param storeId The store ID
     * @return List of all store-specific rules
     */
    @Query("SELECT pr FROM PricingRule pr WHERE pr.store.storeId = :storeId")
    List<PricingRule> findByStoreId(@Param("storeId") Long storeId);

    /**
     * @brief Find all global (network-wide) pricing rules
     * @return List of all global rules
     */
    @Query("SELECT pr FROM PricingRule pr WHERE pr.isGlobal = true AND pr.store IS NULL")
    List<PricingRule> findAllGlobalRules();

    /**
     * @brief Find all pricing rules with a specific promotion type
     * @param promotion The promotion type to filter by
     * @return List of rules with the specified promotion
     */
    List<PricingRule> findByPromotion(PromotionType promotion);

    /**
     * @brief Find all active pricing rules
     * @return List of currently active rules
     */
    @Query("SELECT pr FROM PricingRule pr " +
           "WHERE pr.isActive = true " +
           "AND pr.validFrom <= :now " +
           "AND (pr.validTo IS NULL OR pr.validTo > :now)")
    List<PricingRule> findAllActiveRules(@Param("now") LocalDateTime now);

    /**
     * @brief Check if an active rule exists for item/store combination
     * @param itemId The warehouse item ID
     * @param storeId The store ID (can be null for global)
     * @param now Current timestamp
     * @return true if an active rule exists
     */
    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END FROM PricingRule pr " +
           "WHERE pr.itemId = :itemId " +
           "AND (pr.store.storeId = :storeId OR (pr.isGlobal = true AND pr.store IS NULL)) " +
           "AND pr.isActive = true " +
           "AND pr.validFrom <= :now " +
           "AND (pr.validTo IS NULL OR pr.validTo > :now)")
    boolean existsActiveRuleForItemAndStore(
            @Param("itemId") Integer itemId,
            @Param("storeId") Long storeId,
            @Param("now") LocalDateTime now
    );
}
