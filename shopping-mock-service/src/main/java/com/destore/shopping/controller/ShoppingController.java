package com.destore.shopping.controller;

import com.destore.shopping.model.BasketPricingRequest;
import com.destore.shopping.model.BasketPricingResponse;
import com.destore.shopping.model.CustomerPointsDTO;
import com.destore.shopping.model.RedemptionOfferDTO;
import com.destore.shopping.service.LoyaltyServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Shopping Mock Controller - Test Harness
 * <p>
 * Provides simple test scenarios for loyalty system integration testing.
 * Each scenario button triggers a specific test case.
 * </p>
 *
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@RestController
public class ShoppingController {

    private static final Logger log = LoggerFactory.getLogger(ShoppingController.class);

    private final LoyaltyServiceClient loyaltyServiceClient;
    
    @Value("${mock.customer.default-id}")
    private Integer defaultCustomerId;

    public ShoppingController(LoyaltyServiceClient loyaltyServiceClient) {
        this.loyaltyServiceClient = loyaltyServiceClient;
    }

    /**
     * Get current default customer ID
     *
     * @return default customer ID
     */
    @GetMapping("/test/customer-id")
    @ResponseBody
    public Integer getCurrentCustomerId() {
        return defaultCustomerId;
    }

    /**
     * Set new default customer ID
     *
     * @param customerId new customer ID
     * @return confirmation message
     */
    @PostMapping("/test/customer-id")
    @ResponseBody
    public String setCustomerId(@RequestParam Integer customerId) {
        log.info("Changing default customer ID from {} to {}", defaultCustomerId, customerId);
        defaultCustomerId = customerId;
        return String.format("✅ Customer ID changed to %d", customerId);
    }

    /**
     * Trigger points calculation for a customer
     *
     * @param customerId customer ID
     * @return result message
     */
    @PostMapping("/test/calculate-points")
    @ResponseBody
    public String calculatePoints(@RequestParam Integer customerId) {
        log.info("Test: Calculating points for customer {}", customerId);
        try {
            loyaltyServiceClient.calculateCustomerPoints(customerId);
            CustomerPointsDTO updated = loyaltyServiceClient.getCustomerPoints(customerId);
            return String.format("✅ Points calculated successfully\nCustomer: %d\nBalance: %d points\nLifetime Earned: %d\nLifetime Redeemed: %d",
                customerId, updated.currentBalance, updated.lifetimeEarned, updated.lifetimeRedeemed);
        } catch (Exception e) {
            log.error("Error calculating points", e);
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Get customer balance (prints to terminal via logging)
     *
     * @param customerId customer ID
     * @return JSON response with balance data
     */
    @GetMapping("/test/refresh-balance")
    @ResponseBody
    public CustomerPointsDTO refreshBalance(@RequestParam Integer customerId) {
        log.info("Test: Refreshing balance for customer {}", customerId);
        try {
            CustomerPointsDTO balance = loyaltyServiceClient.getCustomerPoints(customerId);
            log.info("✅ Balance retrieved - Customer: {}, Balance: {}, Lifetime Earned: {}, Lifetime Redeemed: {}",
                customerId, balance.currentBalance, balance.lifetimeEarned, balance.lifetimeRedeemed);
            return balance;
        } catch (Exception e) {
            log.error("❌ Error refreshing balance", e);
            throw e;
        }
    }

    /**
     * Test: Calculate points for non-existent customer
     */
    @PostMapping("/test/edge-case/nonexistent-customer")
    @ResponseBody
    public String testNonexistentCustomer() {
        log.info("Test: Edge Case - Nonexistent Customer");
        try {
            Integer fakeCustomerId = 99999;
            loyaltyServiceClient.calculateCustomerPoints(fakeCustomerId);
            CustomerPointsDTO result = loyaltyServiceClient.getCustomerPoints(fakeCustomerId);
            return String.format("✅ Handled nonexistent customer gracefully\nCustomer: %d\nBalance: %d",
                fakeCustomerId, result.currentBalance);
        } catch (Exception e) {
            log.error("Error in nonexistent customer test", e);
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Test: Calculate with zero transactions
     */
    @PostMapping("/test/edge-case/zero-transactions")
    @ResponseBody
    public String testZeroTransactions() {
        log.info("Test: Edge Case - Customer with Zero Transactions");
        try {
            Integer newCustomerId = 999;
            loyaltyServiceClient.calculateCustomerPoints(newCustomerId);
            CustomerPointsDTO result = loyaltyServiceClient.getCustomerPoints(newCustomerId);
            return String.format("✅ Zero transactions handled correctly\nCustomer: %d\nBalance: %d\nLifetime Earned: %d",
                newCustomerId, result.currentBalance, result.lifetimeEarned);
        } catch (Exception e) {
            log.error("Error in zero transactions test", e);
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Test: Negative customer ID
     */
    @PostMapping("/test/edge-case/negative-customer-id")
    @ResponseBody
    public String testNegativeCustomerId() {
        log.info("Test: Edge Case - Negative Customer ID");
        try {
            Integer negativeId = -1;
            loyaltyServiceClient.calculateCustomerPoints(negativeId);
            CustomerPointsDTO result = loyaltyServiceClient.getCustomerPoints(negativeId);
            return String.format("✅ Negative customer ID handled\nCustomer: %d\nBalance: %d",
                negativeId, result.currentBalance);
        } catch (Exception e) {
            log.error("Error in negative customer ID test", e);
            return "❌ Error (Expected): " + e.getMessage();
        }
    }

    /**
     * Test: Large customer ID
     */
    @PostMapping("/test/edge-case/large-customer-id")
    @ResponseBody
    public String testLargeCustomerId() {
        log.info("Test: Edge Case - Large Customer ID");
        try {
            Integer largeId = Integer.MAX_VALUE;
            loyaltyServiceClient.calculateCustomerPoints(largeId);
            CustomerPointsDTO result = loyaltyServiceClient.getCustomerPoints(largeId);
            return String.format("✅ Large customer ID handled\nCustomer: %d\nBalance: %d",
                largeId, result.currentBalance);
        } catch (Exception e) {
            log.error("Error in large customer ID test", e);
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Test: Redeem with insufficient points
     */
    @PostMapping("/test/redemption/insufficient-points")
    @ResponseBody
    public String testInsufficientPoints() {
        log.info("Test: Redemption - Insufficient Points");
        try {
            // Use customer with low balance and try to redeem expensive offer
            BasketPricingRequest request = new BasketPricingRequest();
            request.customerId = 999; // Customer with 0 points
            request.storeId = 1;
            
            BasketPricingRequest.BasketItem item = new BasketPricingRequest.BasketItem();
            item.itemId = 1;
            item.itemName = "Test Item";
            item.quantity = 1;
            item.unitPrice = new java.math.BigDecimal("100.00");
            
            request.items = java.util.Arrays.asList(item);
            request.selectedOfferIds = java.util.Arrays.asList(1);
            
            BasketPricingResponse response = loyaltyServiceClient.priceBasket(request);
            return String.format("✅ Insufficient points handled\nPoints Deducted: %d\nReference: %s",
                response.totalPointsDeducted, response.basketReference);
        } catch (Exception e) {
            log.error("Error in insufficient points test", e);
            return "❌ Error (Expected): " + e.getMessage();
        }
    }

    /**
     * Test: Redeem with invalid offer ID
     */
    @PostMapping("/test/redemption/invalid-offer")
    @ResponseBody
    public String testInvalidOffer() {
        log.info("Test: Redemption - Invalid Offer ID");
        try {
            BasketPricingRequest request = new BasketPricingRequest();
            request.customerId = defaultCustomerId;
            request.storeId = 1;
            
            BasketPricingRequest.BasketItem item = new BasketPricingRequest.BasketItem();
            item.itemId = 1;
            item.itemName = "Test Item";
            item.quantity = 1;
            item.unitPrice = new java.math.BigDecimal("50.00");
            
            request.items = java.util.Arrays.asList(item);
            request.selectedOfferIds = java.util.Arrays.asList(99999);
            
            BasketPricingResponse response = loyaltyServiceClient.priceBasket(request);
            return String.format("❌ Should have failed but got: Points Deducted: %d\nReference: %s",
                response.totalPointsDeducted, response.basketReference);
        } catch (Exception e) {
            log.info("Invalid offer correctly rejected");
            return "✅ Invalid offer rejected: " + e.getMessage();
        }
    }

    /**
     * Test: Successful redemption
     */
    @PostMapping("/test/redemption/successful")
    @ResponseBody
    public String testSuccessfulRedemption() {
        log.info("Test: Redemption - Successful");
        try {
            // Ensure customer has points
            loyaltyServiceClient.calculateCustomerPoints(defaultCustomerId);
            CustomerPointsDTO balance = loyaltyServiceClient.getCustomerPoints(defaultCustomerId);
            
            BasketPricingRequest request = new BasketPricingRequest();
            request.customerId = defaultCustomerId;
            request.storeId = 1;
            
            BasketPricingRequest.BasketItem item1 = new BasketPricingRequest.BasketItem();
            item1.itemId = 1;
            item1.itemName = "Test Item 1";
            item1.quantity = 2;
            item1.unitPrice = new java.math.BigDecimal("25.00");
            
            BasketPricingRequest.BasketItem item2 = new BasketPricingRequest.BasketItem();
            item2.itemId = 2;
            item2.itemName = "Test Item 2";
            item2.quantity = 1;
            item2.unitPrice = new java.math.BigDecimal("15.00");
            
            request.items = java.util.Arrays.asList(item1, item2);
            request.selectedOfferIds = java.util.Arrays.asList(1);
            
            BasketPricingResponse response = loyaltyServiceClient.priceBasket(request);
            CustomerPointsDTO newBalance = loyaltyServiceClient.getCustomerPoints(defaultCustomerId);
            
            return String.format("✅ Redemption successful\nBefore: %d points\nDeducted: %d points\nAfter: %d points\nReference: %s",
                balance.currentBalance, response.totalPointsDeducted, newBalance.currentBalance, response.basketReference);
        } catch (Exception e) {
            log.error("Error in successful redemption test", e);
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Test: Multiple offers redemption
     */
    @PostMapping("/test/redemption/multiple-offers")
    @ResponseBody
    public String testMultipleOffers() {
        log.info("Test: Redemption - Multiple Offers");
        try {
            // Ensure customer has points
            loyaltyServiceClient.calculateCustomerPoints(defaultCustomerId);
            
            BasketPricingRequest request = new BasketPricingRequest();
            request.customerId = defaultCustomerId;
            request.storeId = 1;
            
            BasketPricingRequest.BasketItem item1 = new BasketPricingRequest.BasketItem();
            item1.itemId = 1;
            item1.itemName = "Test Item 1";
            item1.quantity = 3;
            item1.unitPrice = new java.math.BigDecimal("30.00");
            
            BasketPricingRequest.BasketItem item2 = new BasketPricingRequest.BasketItem();
            item2.itemId = 2;
            item2.itemName = "Test Item 2";
            item2.quantity = 2;
            item2.unitPrice = new java.math.BigDecimal("20.00");
            
            request.items = java.util.Arrays.asList(item1, item2);
            request.selectedOfferIds = java.util.Arrays.asList(1, 2);
            
            BasketPricingResponse response = loyaltyServiceClient.priceBasket(request);
            return String.format("✅ Multiple offers redeemed\nPoints Deducted: %d\nItems: %d\nReference: %s",
                response.totalPointsDeducted, response.itemPrices.size(), response.basketReference);
        } catch (Exception e) {
            log.error("Error in multiple offers test", e);
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Test: Empty basket redemption
     */
    @PostMapping("/test/redemption/empty-basket")
    @ResponseBody
    public String testEmptyBasket() {
        log.info("Test: Redemption - Empty Basket");
        try {
            BasketPricingRequest request = new BasketPricingRequest();
            request.customerId = defaultCustomerId;
            request.storeId = 1;
            request.items = java.util.Arrays.asList();
            request.selectedOfferIds = java.util.Arrays.asList(1);
            
            BasketPricingResponse response = loyaltyServiceClient.priceBasket(request);
            return String.format("❌ Should have failed but got: Points Deducted: %d",
                response.totalPointsDeducted);
        } catch (Exception e) {
            log.info("Empty basket correctly rejected");
            return "✅ Empty basket rejected: " + e.getMessage();
        }
    }
}
