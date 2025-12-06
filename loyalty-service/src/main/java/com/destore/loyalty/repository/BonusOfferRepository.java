package com.destore.loyalty.repository;

import com.destore.loyalty.entity.BonusOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for BonusOffer entity operations.
 * <p>
 * Provides data access methods for managing bonus offer rules.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Repository
public interface BonusOfferRepository extends JpaRepository<BonusOffer, Integer> {

    /**
     * Finds all active bonus offers.
     *
     * @return List of active bonus offers
     */
    List<BonusOffer> findByIsActiveTrue();

    /**
     * Finds all active bonus offers at the given time.
     *
     * @param now Current timestamp for validating offer validity period
     * @return List of currently active and valid bonus offers
     */
    @Query("SELECT b FROM BonusOffer b WHERE b.isActive = true AND b.validFrom <= :now AND (b.validTo IS NULL OR b.validTo > :now)")
    List<BonusOffer> findActiveOffers(@Param("now") LocalDateTime now);

    /**
     * Finds bonus offers valid at a specific timestamp and store.
     * <p>
     * Returns global offers and store-specific offers, ordered by specificity.
     * </p>
     *
     * @param storeId Store ID
     * @param timestamp Transaction timestamp
     * @return List of applicable bonus offers
     */
    @Query("SELECT b FROM BonusOffer b WHERE " +
           "(b.storeId IS NULL OR b.storeId = :storeId) " +
           "AND b.isActive = true " +
           "AND b.validFrom <= :timestamp " +
           "AND (b.validTo IS NULL OR b.validTo > :timestamp) " +
           "ORDER BY b.thresholdAmount DESC")
    List<BonusOffer> findApplicableBonuses(@Param("storeId") Integer storeId,
                                           @Param("timestamp") LocalDateTime timestamp);

    /**
     * Finds active bonus offers for a specific store.
     *
     * @param storeId Store ID
     * @return List of store-specific active bonus offers
     */
    List<BonusOffer> findByStoreIdAndIsActiveTrue(Integer storeId);
}
