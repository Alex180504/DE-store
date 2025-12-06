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
 * @file PriceCalculationEngine.java
 * @brief Core pricing calculation engine with hierarchical rule support
 * 
 * This engine implements the hierarchical pricing logic where store-specific
 * rules override global (network-wide) rules. The calculation process:
 * 
 * 1. Validate item exists in warehouse database
 * 2. Query for store-specific rule (if storeId provided)
 * 3. If no store rule found, fallback to global rule
 * 4. Apply promotion discounts using PromotionEngine
 * 5. Return detailed price breakdown
 * 
 * The override logic ensures that Store Managers can set custom pricing
 * that takes precedence over Network Manager's global pricing.
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
     * @brief Calculate final price for an item with quantity and promotions
     * 
     * Algorithm:
     * 1. Validate item exists in warehouse
     * 2. Find applicable pricing rule (store-specific or global)
     * 3. Calculate subtotal = unitPrice * quantity
     * 4. Calculate promotion discount
     * 5. Calculate final price = subtotal - discount
     * 6. Return detailed breakdown
     * 
     * Hierarchical Rule Logic:
     * - If storeId is provided, search for store-specific rule first
     * - If no store rule found, fallback to global rule
     * - Store-specific rules (is_global=false) override global rules (is_global=true)
     * 
     * @param request The price calculation request (itemId, storeId, quantity)
     * @return Detailed price calculation response
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
     * @brief Find the applicable pricing rule using hierarchical logic
     * 
     * Hierarchical Override Algorithm:
     * 1. If storeId provided:
     *    a. Query for active rules matching itemId AND storeId
     *    b. Query for active global rules matching itemId
     *    c. Prioritize store-specific rule over global rule
     * 2. If no storeId provided:
     *    a. Query only for global rules
     * 3. If no rule found, throw exception
     * 
     * The database query returns results ordered by is_global (false first),
     * so store-specific rules naturally appear before global rules.
     * 
     * @param itemId The warehouse item ID
     * @param storeId The store ID (can be null for global pricing)
     * @return The applicable pricing rule
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
     * @brief Check if a pricing rule exists for an item/store combination
     * 
     * Utility method to verify if pricing is configured before attempting
     * to calculate prices.
     * 
     * @param itemId The warehouse item ID
     * @param storeId The store ID (can be null)
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
