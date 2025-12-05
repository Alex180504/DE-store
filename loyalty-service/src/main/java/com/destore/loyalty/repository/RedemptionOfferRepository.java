package com.destore.loyalty.repository;

import com.destore.loyalty.entity.RedemptionOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RedemptionOffer entity operations.
 * <p>
 * Provides data access methods for managing redemption offers with
 * optimistic locking support for concurrent usage tracking.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Repository
public interface RedemptionOfferRepository extends JpaRepository<RedemptionOffer, Integer> {

    /**
     * Finds all active redemption offers.
     *
     * @return List of active redemption offers
     */
    List<RedemptionOffer> findByIsActiveTrue();

    /**
     * Finds active redemption offers currently valid.
     *
     * @param now Current timestamp
     * @return List of currently valid offers
     */
    @Query("SELECT r FROM RedemptionOffer r WHERE r.isActive = true " +
           "AND r.validFrom <= :now " +
           "AND (r.validTo IS NULL OR r.validTo > :now) " +
           "AND (r.maxTotalUses IS NULL OR r.currentTotalUses < r.maxTotalUses)")
    List<RedemptionOffer> findActiveAvailableOffers(@Param("now") LocalDateTime now);

    /**
     * Finds redemption offers applicable to a specific item.
     *
     * @param itemId Item ID (null matches global offers)
     * @param now Current timestamp
     * @return List of applicable offers
     */
    @Query("SELECT r FROM RedemptionOffer r WHERE r.isActive = true " +
           "AND (r.itemId IS NULL OR r.itemId = :itemId) " +
           "AND r.validFrom <= :now " +
           "AND (r.validTo IS NULL OR r.validTo > :now) " +
           "AND (r.maxTotalUses IS NULL OR r.currentTotalUses < r.maxTotalUses)")
    List<RedemptionOffer> findApplicableOffers(@Param("itemId") Integer itemId,
                                                @Param("now") LocalDateTime now);

    /**
     * Finds redemption offer with pessimistic write lock for SAGA pattern.
     * <p>
     * Used during basket checkout to reserve points atomically.
     * </p>
     *
     * @param redemptionId Offer ID
     * @return Optional containing the locked offer
     */
    @Query("SELECT r FROM RedemptionOffer r WHERE r.redemptionId = :redemptionId")
    Optional<RedemptionOffer> findByIdForUpdate(@Param("redemptionId") Integer redemptionId);
}
