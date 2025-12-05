package com.destore.pricing.service;

import com.destore.pricing.exception.ItemNotFoundException;
import com.destore.pricing.exception.PricingRuleNotFoundException;
import com.destore.pricing.model.dto.PriceCalculationRequest;
import com.destore.pricing.model.dto.PriceCalculationResponse;
import com.destore.pricing.model.entity.PricingRule;
import com.destore.pricing.repository.PricingRuleRepository;
import com.destore.pricing.repository.WarehouseItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core pricing calculation engine with hierarchical rule support.
 * <p>
 * This engine implements the hierarchical pricing logic where store-specific
 * rules override global (network-wide) rules. The calculation process:
 * <ol>
 *   <li>Validate item exists in warehouse database</li>
 *   <li>Query for store-specific rule (if storeId provided)</li>
 *   <li>If no store rule found, fallback to global rule</li>
 *   <li>Apply promotion discounts using PromotionEngine</li>
 *   <li>Return detailed price breakdown</li>
 * </ol>
 * </p>
 * <p>
 * The override logic ensures that Store Managers can set custom pricing
 * that takes precedence over Network Manager's global pricing.
 * </p>
 *
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceCalculationEngine {

    private final PricingRuleRepository pricingRuleRepository;
    private final WarehouseItemRepository warehouseItemRepository;
    private final PromotionEngine promotionEngine;

    /**
     * Calculates the final price for an item with quantity and promotions.
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Validate item exists in warehouse</li>
     *   <li>Find applicable pricing rule (store-specific or global)</li>
     *   <li>Calculate subtotal = unitPrice * quantity</li>
     *   <li>Calculate promotion discount</li>
     *   <li>Calculate final price = subtotal - discount</li>
     *   <li>Return detailed breakdown</li>
     * </ol>
     * </p>
     * <p>
     * Hierarchical Rule Logic:
     * <ul>
     *   <li>If storeId is provided, search for store-specific rule first</li>
     *   <li>If no store rule found, fallback to global rule</li>
     *   <li>Store-specific rules (is_global=false) override global rules (is_global=true)</li>
     * </ul>
     * </p>
     *
     * @param request the price calculation request (itemId, storeId, quantity)
     * @return the detailed {@link PriceCalculationResponse}
     * @throws ItemNotFoundException if item doesn't exist in warehouse
     * @throws PricingRuleNotFoundException if no pricing rule found
     */
    @Transactional(readOnly = true)
    public PriceCalculationResponse calculatePrice(PriceCalculationRequest request) {
        Integer itemId = request.getItemId();
        Integer storeId = request.getStoreId();
        Integer quantity = request.getQuantity();

        log.info("Calculating price: itemId={}, storeId={}, quantity={}", itemId, storeId, quantity);

        // Step 1: Validate item exists
        if (!warehouseItemRepository.existsById(itemId)) {
            log.error("Item not found in warehouse: itemId={}", itemId);
            throw new ItemNotFoundException(itemId);
        }

        // Step 2: Find applicable pricing rule (hierarchical lookup)
        PricingRule rule = findApplicableRule(itemId, storeId);

        // Step 3: Calculate prices
        BigDecimal unitPrice = rule.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountAmount = promotionEngine.calculateDiscount(rule, unitPrice, quantity);
        BigDecimal finalPrice = subtotal.subtract(discountAmount);

        // Step 4: Determine rule source
        boolean isStoreSpecific = rule.getStoreId() != null && !rule.getIsGlobal();
        String ruleSource = isStoreSpecific ? "STORE_SPECIFIC" : "GLOBAL";

        // Step 5: Get item name from warehouse
        String itemName = warehouseItemRepository.getItemName(itemId)
                .orElse("Item #" + itemId);

        log.info("Price calculated: itemId={}, finalPrice={}, discount={}, rule={}, source={}",
                 itemId, finalPrice, discountAmount, rule.getRuleId(), ruleSource);

        return PriceCalculationResponse.builder()
                .itemId(itemId)
                .itemName(itemName)
                .storeId(storeId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .promotionApplied(rule.getPromotion())
                .promotionValue(rule.getPromotionValue())
                .discount(discountAmount)
                .finalPrice(finalPrice)
                .ruleSource(ruleSource)
                .appliedRuleId(rule.getRuleId())
                .build();
    }

    /**
     * Finds the applicable pricing rule using hierarchical logic.
     * <p>
     * Hierarchical Override Algorithm:
     * <ol>
     *   <li>If storeId provided:
     *     <ol type="a">
     *       <li>Query for active rules matching itemId AND storeId</li>
     *       <li>Query for active global rules matching itemId</li>
     *       <li>Prioritize store-specific rule over global rule</li>
     *     </ol>
     *   </li>
     *   <li>If no storeId provided:
     *     <ol type="a">
     *       <li>Query only for global rules</li>
     *     </ol>
     *   </li>
     *   <li>If no rule found, throw exception</li>
     * </ol>
     * </p>
     * <p>
     * The database query returns results ordered by is_global (false first),
     * so store-specific rules naturally appear before global rules.
     * </p>
     *
     * @param itemId the warehouse item ID
     * @param storeId the store ID (can be null for global pricing)
     * @return the applicable {@link PricingRule}
     * @throws PricingRuleNotFoundException if no rule found
     */
    private PricingRule findApplicableRule(Integer itemId, Integer storeId) {
        LocalDateTime now = LocalDateTime.now();

        if (storeId != null) {
            // Look for store-specific or global rules
            List<PricingRule> rules = pricingRuleRepository.findActiveRulesForItemAndStore(
                itemId, storeId, now
            );

            if (!rules.isEmpty()) {
                // First rule will be store-specific (if exists), then global
                PricingRule selectedRule = rules.get(0);
                log.debug("Found {} rules for itemId={}, storeId={}. Selected ruleId={} (isGlobal={})",
                         rules.size(), itemId, storeId, selectedRule.getRuleId(), selectedRule.getIsGlobal());
                return selectedRule;
            }
        }

        // Fallback to global rule
        return pricingRuleRepository.findActiveGlobalRuleForItem(itemId, now)
                .orElseThrow(() -> {
                    log.error("No pricing rule found: itemId={}, storeId={}", itemId, storeId);
                    return new PricingRuleNotFoundException(itemId, storeId);
                });
    }

    /**
     * Checks if a pricing rule exists for an item/store combination.
     * <p>
     * Utility method to verify if pricing is configured before attempting
     * to calculate prices.
     * </p>
     *
     * @param itemId the warehouse item ID
     * @param storeId the store ID (can be null)
     * @return true if an active pricing rule exists
     */
    public boolean hasPricingRule(Integer itemId, Integer storeId) {
        LocalDateTime now = LocalDateTime.now();
        
        if (storeId != null) {
            return pricingRuleRepository.existsActiveRuleForItemAndStore(itemId, storeId, now);
        } else {
            return pricingRuleRepository.findActiveGlobalRuleForItem(itemId, now).isPresent();
        }
    }
}
