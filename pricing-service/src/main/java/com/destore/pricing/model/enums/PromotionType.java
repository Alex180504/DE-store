package com.destore.pricing.model.enums;

/**
 * @file PromotionType.java
 * @brief Enumeration of supported promotion types
 * 
 * Defines the various promotional pricing strategies available in the system.
 * Each promotion type requires different calculation logic in the PromotionEngine.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
public enum PromotionType {
    /**
     * No promotion applied - regular pricing
     */
    NONE,
    
    /**
     * Buy 3, pay for 2 promotion
     * Customer pays for 2 items when purchasing 3
     */
    THREE_FOR_TWO,
    
    /**
     * Buy One Get One Free
     * Customer gets second item free when purchasing 2
     */
    BOGOF,
    
    /**
     * Free delivery charges
     * Delivery cost waived for this item
     */
    FREE_DELIVERY,
    
    /**
     * Percentage discount
     * Requires promotion_value field (e.g., 10.00 for 10% off)
     */
    PERCENTAGE_OFF,
    
    /**
     * Fixed amount discount
     * Requires promotion_value field (e.g., 5.00 for £5 off)
     */
    FIXED_DISCOUNT
}
