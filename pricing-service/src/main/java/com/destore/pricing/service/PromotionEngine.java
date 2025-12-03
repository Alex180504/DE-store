package com.destore.pricing.service;

import com.destore.pricing.model.entity.PricingRule;
import com.destore.pricing.model.enums.PromotionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @file PromotionEngine.java
 * @brief Service for calculating promotional discounts
 * 
 * This engine implements various promotion types including:
 * - THREE_FOR_TWO: Buy 3, pay for 2
 * - BOGOF: Buy One Get One Free
 * - FREE_DELIVERY: Waive delivery charges (noted but not calculated here)
 * - PERCENTAGE_OFF: X% discount
 * - FIXED_DISCOUNT: Fixed amount off
 * 
 * All calculations use BigDecimal for precision and round to 2 decimal places.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class PromotionEngine {

    /**
     * @brief Calculate discount amount based on promotion type
     * 
     * Algorithm:
     * 1. If promotion is NONE, return zero discount
     * 2. For quantity-based promotions (3FOR2, BOGOF), calculate free items
     * 3. For value-based promotions (PERCENTAGE_OFF, FIXED_DISCOUNT), apply to subtotal
     * 4. Round result to 2 decimal places
     * 
     * @param rule The pricing rule containing promotion details
     * @param unitPrice The unit price of the item
     * @param quantity The quantity being purchased
     * @return Discount amount (always >= 0)
     */
    public BigDecimal calculateDiscount(PricingRule rule, BigDecimal unitPrice, Integer quantity) {
        PromotionType promotion = rule.getPromotion();
        
        if (promotion == null || promotion == PromotionType.NONE) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = switch (promotion) {
            case THREE_FOR_TWO -> calculateThreeForTwo(unitPrice, quantity);
            case BOGOF -> calculateBogof(unitPrice, quantity);
            case FREE_DELIVERY -> BigDecimal.ZERO; // Noted in response, not calculated here
            case PERCENTAGE_OFF -> calculatePercentageOff(unitPrice, quantity, rule.getPromotionValue());
            case FIXED_DISCOUNT -> calculateFixedDiscount(unitPrice, quantity, rule.getPromotionValue());
            default -> BigDecimal.ZERO;
        };

        // Ensure discount doesn't exceed subtotal
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (discount.compareTo(subtotal) > 0) {
            log.warn("Discount {} exceeds subtotal {}. Capping discount.", discount, subtotal);
            discount = subtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @brief Calculate Three-for-Two promotion discount
     * 
     * Algorithm:
     * - For every 3 items purchased, customer pays for only 2
     * - freeItems = quantity / 3
     * - discount = freeItems * unitPrice
     * 
     * Examples:
     * - Quantity 3: Pay for 2, get 1 free
     * - Quantity 5: Pay for 4, get 1 free (not enough for 2 sets)
     * - Quantity 6: Pay for 4, get 2 free
     * 
     * @param unitPrice Unit price of the item
     * @param quantity Quantity being purchased
     * @return Discount amount
     */
    private BigDecimal calculateThreeForTwo(BigDecimal unitPrice, Integer quantity) {
        int freeItems = quantity / 3;
        BigDecimal discount = unitPrice.multiply(BigDecimal.valueOf(freeItems));
        log.debug("3FOR2: quantity={}, freeItems={}, discount={}", quantity, freeItems, discount);
        return discount;
    }

    /**
     * @brief Calculate Buy-One-Get-One-Free promotion discount
     * 
     * Algorithm:
     * - For every 2 items purchased, customer pays for only 1
     * - freeItems = quantity / 2
     * - discount = freeItems * unitPrice
     * 
     * Examples:
     * - Quantity 2: Pay for 1, get 1 free
     * - Quantity 3: Pay for 2, get 1 free
     * - Quantity 4: Pay for 2, get 2 free
     * 
     * @param unitPrice Unit price of the item
     * @param quantity Quantity being purchased
     * @return Discount amount
     */
    private BigDecimal calculateBogof(BigDecimal unitPrice, Integer quantity) {
        int freeItems = quantity / 2;
        BigDecimal discount = unitPrice.multiply(BigDecimal.valueOf(freeItems));
        log.debug("BOGOF: quantity={}, freeItems={}, discount={}", quantity, freeItems, discount);
        return discount;
    }

    /**
     * @brief Calculate percentage-off discount
     * 
     * Algorithm:
     * - discount = (subtotal * percentage) / 100
     * - subtotal = unitPrice * quantity
     * 
     * Example:
     * - Price £79.99, Quantity 3, 10% off
     * - Subtotal = £239.97
     * - Discount = £23.99
     * 
     * @param unitPrice Unit price of the item
     * @param quantity Quantity being purchased
     * @param percentage Percentage to discount (e.g., 10.00 for 10%)
     * @return Discount amount
     */
    private BigDecimal calculatePercentageOff(BigDecimal unitPrice, Integer quantity, BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid percentage value: {}", percentage);
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = subtotal.multiply(percentage)
                                      .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        log.debug("PERCENTAGE_OFF: subtotal={}, percentage={}%, discount={}", 
                  subtotal, percentage, discount);
        return discount;
    }

    /**
     * @brief Calculate fixed discount amount
     * 
     * Algorithm:
     * - discount = min(fixedAmount, subtotal)
     * - Ensures discount doesn't exceed total price
     * 
     * Example:
     * - Price £139.99, Quantity 1, £20 off
     * - Discount = £20.00
     * 
     * @param unitPrice Unit price of the item
     * @param quantity Quantity being purchased
     * @param fixedAmount Fixed amount to discount
     * @return Discount amount
     */
    private BigDecimal calculateFixedDiscount(BigDecimal unitPrice, Integer quantity, BigDecimal fixedAmount) {
        if (fixedAmount == null || fixedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid fixed discount value: {}", fixedAmount);
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = fixedAmount.min(subtotal);
        
        log.debug("FIXED_DISCOUNT: subtotal={}, fixedAmount={}, discount={}", 
                  subtotal, fixedAmount, discount);
        return discount;
    }

    /**
     * @brief Generate human-readable description of promotion
     * 
     * Creates a user-friendly explanation of the applied promotion.
     * 
     * @param promotion The promotion type
     * @param promotionValue The promotion value (for percentage/fixed)
     * @param quantity The quantity purchased
     * @return Description string
     */
    public String getPromotionDescription(PromotionType promotion, BigDecimal promotionValue, Integer quantity) {
        return switch (promotion) {
            case THREE_FOR_TWO -> String.format("3 for 2 promotion: %d free items", quantity / 3);
            case BOGOF -> String.format("Buy One Get One Free: %d free items", quantity / 2);
            case FREE_DELIVERY -> "Free delivery on this item";
            case PERCENTAGE_OFF -> String.format("%.0f%% discount applied", promotionValue);
            case FIXED_DISCOUNT -> String.format("£%.2f discount applied", promotionValue);
            case NONE -> "No promotion";
        };
    }
}
