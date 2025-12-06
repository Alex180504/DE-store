package com.destore.pricing.exception;

/**
 * @file PricingRuleNotFoundException.java
 * @brief Exception thrown when a pricing rule is not found
 * 
 * This exception is raised when attempting to retrieve, update, or delete
 * a pricing rule that doesn't exist, or when no applicable pricing rule
 * can be found for a given item/store combination.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
public class PricingRuleNotFoundException extends RuntimeException {

    /**
     * @brief Construct exception with rule ID
     * @param ruleId The pricing rule ID that was not found
     */
    public PricingRuleNotFoundException(Integer ruleId) {
        super("Pricing rule not found: ruleId=" + ruleId);
    }

    /**
     * @brief Construct exception for item/store combination
     * @param itemId The item ID
     * @param storeId The store ID (can be null for global)
     */
    public PricingRuleNotFoundException(Integer itemId, Integer storeId) {
        super(String.format("No pricing rule found for itemId=%d, storeId=%s", 
              itemId, storeId != null ? storeId.toString() : "global"));
    }

    /**
     * @brief Construct exception with custom message
     * @param message The error message
     */
    public PricingRuleNotFoundException(String message) {
        super(message);
    }
}
