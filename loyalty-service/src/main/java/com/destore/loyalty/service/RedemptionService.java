package com.destore.loyalty.service;

import com.destore.loyalty.dto.BasketPricingRequest;
import com.destore.loyalty.dto.BasketPricingResponse;
import com.destore.loyalty.entity.*;
import com.destore.loyalty.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling loyalty points redemption.
 * <p>
 * Implements SAGA pattern for basket checkout with points redemption:
 * 1. Validate customer has sufficient points
 * 2. Reserve points (pessimistic lock)
 * 3. Calculate discounted prices
 * 4. Commit transaction
 * 5. Compensate on failure (refund points)
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final CustomerPointsBalanceRepository balanceRepository;
    private final RedemptionOfferRepository offerRepository;
    private final RedemptionUsageRepository usageRepository;
    private final PointsTransactionRepository transactionRepository;

    /**
     * Prices a basket with loyalty point redemption.
     * <p>
     * SAGA pattern implementation:
     * - Validates customer balance and offer availability
     * - Reserves points with pessimistic locking
     * - Calculates discounted prices
     * - Records transactions for audit trail
     * - Handles compensation on failure
     * </p>
     *
     * @param request Basket pricing request
     * @return Pricing response with discounted prices
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public BasketPricingResponse priceBasketWithRedemption(BasketPricingRequest request) {
        log.info("Pricing basket {} for customer {} with {} offers",
                request.getBasketReference(), request.getCustomerId(), 
                request.getSelectedOfferIds().size());

        try {
            // SAGA Step 1: Validate and Reserve Points
            CustomerPointsBalance balance = validateAndReservePoints(request);

            // SAGA Step 2: Calculate Discounted Prices
            BasketPricingResponse response = calculateDiscountedPrices(request, balance);

            // SAGA Step 3: Record Redemption Transactions
            recordRedemptionTransactions(request, response);

            // SAGA Step 4: Update Offer Usage Counts
            updateOfferUsage(request);

            log.info("Successfully priced basket {} with {} points deducted",
                    request.getBasketReference(), response.getTotalPointsDeducted());

            return response;

        } catch (Exception e) {
            log.error("Error pricing basket {}: {}", request.getBasketReference(), e.getMessage(), e);
            // SAGA Compensation handled by transaction rollback
            throw e;
        }
    }

    /**
     * SAGA Step 1: Validates customer has sufficient points and reserves them.
     *
     * @param request Basket pricing request
     * @return Customer balance with reserved points
     * @throws IllegalArgumentException if insufficient points or invalid offers
     */
    private CustomerPointsBalance validateAndReservePoints(BasketPricingRequest request) {
        // Acquire pessimistic lock on customer balance
        CustomerPointsBalance balance = balanceRepository
                .findByCustomerIdForUpdate(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer balance not found: " + request.getCustomerId()));

        // Calculate total points cost
        BigDecimal totalPointsCost = BigDecimal.ZERO;
        List<RedemptionOffer> offers = new ArrayList<>();

        for (Integer offerId : request.getSelectedOfferIds()) {
            RedemptionOffer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));

            // Validate offer is active and available
            if (!offer.isValidAt(LocalDateTime.now())) {
                throw new IllegalArgumentException("Offer expired or inactive: " + offerId);
            }

            if (!offer.hasAvailableUses()) {
                throw new IllegalArgumentException("Offer usage limit reached: " + offerId);
            }

            // Check customer-specific usage limits
            Optional<RedemptionUsage> usage = usageRepository
                    .findByCustomerIdAndRedemptionId(request.getCustomerId(), offerId);
            
            if (usage.isPresent() && !usage.get().canUse(offer.getMaxUsesPerCustomer())) {
                throw new IllegalArgumentException(
                        "Customer usage limit reached for offer: " + offerId);
            }

            totalPointsCost = totalPointsCost.add(offer.getPointsCost());
            offers.add(offer);
        }

        // Validate sufficient balance
        if (!balance.hasSufficientPoints(totalPointsCost)) {
            throw new IllegalArgumentException(
                    String.format("Insufficient points. Required: %s, Available: %s",
                            totalPointsCost, balance.getCurrentBalance()));
        }

        // Reserve points (deduct from balance)
        balance.debitPoints(totalPointsCost);
        balanceRepository.save(balance);

        log.info("Reserved {} points for customer {}", totalPointsCost, request.getCustomerId());

        return balance;
    }

    /**
     * SAGA Step 2: Calculates discounted prices for basket items.
     *
     * @param request Basket pricing request
     * @param balance Customer balance after reservation
     * @return Pricing response with discounts applied
     */
    private BasketPricingResponse calculateDiscountedPrices(
            BasketPricingRequest request, CustomerPointsBalance balance) {

        List<BasketPricingResponse.BasketItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalPointsUsed = BigDecimal.ZERO;

        LocalDateTime now = LocalDateTime.now();

        for (BasketPricingRequest.BasketItemRequest item : request.getItems()) {
            BigDecimal originalPrice = item.getUnitPrice();
            BigDecimal discountedPrice = originalPrice;
            Integer appliedOfferId = null;
            String appliedOfferName = null;
            BigDecimal itemDiscount = BigDecimal.ZERO;

            // Find applicable offer for this item
            for (Integer offerId : request.getSelectedOfferIds()) {
                RedemptionOffer offer = offerRepository.findById(offerId).orElse(null);
                
                if (offer != null && offer.appliesToItem(item.getItemId())) {
                    
                    // Apply discount
                    BigDecimal discountPercent = offer.getDiscountPercentage()
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    BigDecimal discountAmount = originalPrice.multiply(discountPercent);
                    discountedPrice = originalPrice.subtract(discountAmount);

                    itemDiscount = discountAmount.multiply(BigDecimal.valueOf(item.getQuantity()));
                    totalDiscount = totalDiscount.add(itemDiscount);
                    totalPointsUsed = totalPointsUsed.add(offer.getPointsCost());

                    appliedOfferId = offer.getRedemptionId();
                    appliedOfferName = offer.getOfferName();

                    log.debug("Applied offer {} to item {}: {}% discount",
                            offer.getOfferName(), item.getItemId(), offer.getDiscountPercentage());

                    break; // Only one offer per item
                }
            }

            BasketPricingResponse.BasketItemResponse itemResponse = 
                    BasketPricingResponse.BasketItemResponse.builder()
                    .itemId(item.getItemId())
                    .itemName(item.getItemName())
                    .quantity(item.getQuantity())
                    .originalUnitPrice(originalPrice)
                    .discountedUnitPrice(discountedPrice)
                    .lineDiscount(itemDiscount)
                    .appliedOfferId(appliedOfferId)
                    .appliedOfferName(appliedOfferName)
                    .build();

            itemResponses.add(itemResponse);
        }

        return BasketPricingResponse.builder()
                .basketReference(request.getBasketReference())
                .totalPointsDeducted(totalPointsUsed)
                .totalDiscountAmount(totalDiscount)
                .remainingBalance(balance.getCurrentBalance())
                .items(itemResponses)
                .build();
    }

    /**
     * SAGA Step 3: Records redemption transactions for audit trail.
     *
     * @param request Basket pricing request
     * @param response Pricing response
     */
    private void recordRedemptionTransactions(
            BasketPricingRequest request, BasketPricingResponse response) {

        for (Integer offerId : request.getSelectedOfferIds()) {
            RedemptionOffer offer = offerRepository.findById(offerId).orElse(null);
            if (offer == null) continue;

            PointsTransaction transaction = PointsTransaction.builder()
                    .customerId(request.getCustomerId())
                    .transactionType(PointsTransaction.TransactionType.REDEEMED)
                    .pointsAmount(offer.getPointsCost().negate()) // Negative for deduction
                    .redemptionId(offerId)
                    .description("Redeemed: " + offer.getOfferName())
                    .basketReference(request.getBasketReference())
                    .build();

            transactionRepository.save(transaction);
        }

        log.info("Recorded {} redemption transactions for basket {}",
                request.getSelectedOfferIds().size(), request.getBasketReference());
    }

    /**
     * SAGA Step 4: Updates offer usage counts.
     *
     * @param request Basket pricing request
     */
    private void updateOfferUsage(BasketPricingRequest request) {
        for (Integer offerId : request.getSelectedOfferIds()) {
            // Update total usage count (with optimistic locking)
            RedemptionOffer offer = offerRepository.findById(offerId).orElse(null);
            if (offer != null) {
                offer.incrementUsage();
                offerRepository.save(offer);
            }

            // Update customer-specific usage
            RedemptionUsage usage = usageRepository
                    .findByCustomerIdAndRedemptionId(request.getCustomerId(), offerId)
                    .orElseGet(() -> RedemptionUsage.builder()
                            .customerId(request.getCustomerId())
                            .redemptionId(offerId)
                            .usageCount(0)
                            .build());

            usage.incrementUsage();
            usageRepository.save(usage);
        }

        log.info("Updated usage counts for {} offers", request.getSelectedOfferIds().size());
    }

    /**
     * SAGA Compensation: Refunds points for a failed basket transaction.
     * <p>
     * Called manually if basket processing fails after points were reserved.
     * </p>
     *
     * @param basketReference Basket reference to compensate
     */
    @Transactional
    public void compensateBasketRedemption(String basketReference) {
        log.warn("Compensating basket redemption for: {}", basketReference);

        List<PointsTransaction> redemptions = transactionRepository
                .findByBasketReference(basketReference);

        if (redemptions.isEmpty()) {
            log.warn("No redemptions found for basket: {}", basketReference);
            return;
        }

        PointsTransaction firstRedemption = redemptions.get(0);
        Integer customerId = firstRedemption.getCustomerId();

        // Calculate total points to refund
        BigDecimal totalRefund = redemptions.stream()
                .map(tx -> tx.getPointsAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Refund points
        CustomerPointsBalance balance = balanceRepository
                .findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new IllegalStateException(
                        "Customer balance not found during compensation: " + customerId));

        balance.creditPoints(totalRefund);
        balanceRepository.save(balance);

        // Record compensation transaction
        PointsTransaction compensation = PointsTransaction.builder()
                .customerId(customerId)
                .transactionType(PointsTransaction.TransactionType.ADJUSTMENT)
                .pointsAmount(totalRefund)
                .description("Refund for failed basket: " + basketReference)
                .basketReference(basketReference)
                .build();
        transactionRepository.save(compensation);

        log.info("Refunded {} points to customer {} for basket {}",
                totalRefund, customerId, basketReference);
    }
}
