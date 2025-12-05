package com.destore.shopping.controller;

import com.destore.shopping.model.CustomerPointsDTO;
import com.destore.shopping.model.RedemptionOfferDTO;
import com.destore.shopping.service.LoyaltyServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
@Controller
public class ShoppingController {

    private static final Logger log = LoggerFactory.getLogger(ShoppingController.class);

    private final LoyaltyServiceClient loyaltyServiceClient;
    
    @Value("${mock.customer.default-id}")
    private Integer defaultCustomerId;

    public ShoppingController(LoyaltyServiceClient loyaltyServiceClient) {
        this.loyaltyServiceClient = loyaltyServiceClient;
    }

    /**
     * Display test scenarios home page
     *
     * @param customerId optional customer ID parameter
     * @param model Spring MVC model
     * @return view name
     */
    @GetMapping("/")
    public String home(@RequestParam(required = false) Integer customerId, Model model) {
        Integer activeCustomerId = customerId != null ? customerId : defaultCustomerId;
        
        log.info("Loading test scenarios page for customer {}", activeCustomerId);
        
        // Get active loyalty offers for context
        List<RedemptionOfferDTO> offers = loyaltyServiceClient.getActiveOffers();
        
        // Get customer points balance
        CustomerPointsDTO customerPoints = loyaltyServiceClient.getCustomerPoints(activeCustomerId);
        
        model.addAttribute("offers", offers);
        model.addAttribute("customerPoints", customerPoints);
        model.addAttribute("customerId", activeCustomerId);
        
        return "test-scenarios";
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
                customerId, updated.getCurrentBalance(), updated.getLifetimeEarned(), updated.getLifetimeRedeemed());
        } catch (Exception e) {
            log.error("Error calculating points", e);
            return "❌ Error: " + e.getMessage();
        }
    }
}
