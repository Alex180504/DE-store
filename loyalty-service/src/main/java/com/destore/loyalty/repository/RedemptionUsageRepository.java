package com.destore.loyalty.repository;

import com.destore.loyalty.entity.RedemptionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RedemptionUsage entity operations.
 * <p>
 * Provides data access methods for tracking customer usage of redemption offers.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Repository
public interface RedemptionUsageRepository extends JpaRepository<RedemptionUsage, Long> {

    /**
     * Finds usage record for a specific customer and offer combination.
     *
     * @param customerId Customer ID
     * @param redemptionId Redemption offer ID
     * @return Optional containing usage record if found
     */
    Optional<RedemptionUsage> findByCustomerIdAndRedemptionId(Long customerId, Long redemptionId);

    /**
     * Finds all redemption usage records for a customer.
     *
     * @param customerId Customer ID
     * @return List of usage records
     */
    List<RedemptionUsage> findByCustomerId(Long customerId);

    /**
     * Finds all usage records for a specific offer.
     *
     * @param redemptionId Redemption offer ID
     * @return List of usage records
     */
    List<RedemptionUsage> findByRedemptionId(Long redemptionId);

    /**
     * Counts total unique customers who have used an offer.
     *
     * @param redemptionId Redemption offer ID
     * @return Count of unique customers
     */
    long countByRedemptionId(Long redemptionId);
}
