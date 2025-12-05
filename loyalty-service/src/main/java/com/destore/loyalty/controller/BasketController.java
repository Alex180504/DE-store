package com.destore.loyalty.controller;

import com.destore.loyalty.dto.BasketPricingRequest;
import com.destore.loyalty.dto.BasketPricingResponse;
import com.destore.loyalty.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for basket pricing with loyalty redemption.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/loyalty/basket")
@RequiredArgsConstructor
public class BasketController {

    private final RedemptionService redemptionService;

    /**
     * Prices a basket with loyalty point redemption.
     * <p>
     * Implements SAGA pattern for consistent point deduction and pricing.
     * </p>
     *
     * @param request Basket pricing request
     * @return Pricing response with discounts
     */
    @PostMapping("/price")
    public ResponseEntity<?> priceBasket(@RequestBody BasketPricingRequest request) {
        log.info("Pricing basket request for customer {}", request.getCustomerId());

        try {
            BasketPricingResponse response = redemptionService.priceBasketWithRedemption(request);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Basket pricing validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            log.error("Error pricing basket: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing basket: " + e.getMessage());
        }
    }

    /**
     * Compensates a basket redemption (SAGA compensation).
     *
     * @param basketReference Basket reference to refund
     * @return Success response
     */
    @PostMapping("/compensate/{basketReference}")
    public ResponseEntity<String> compensateBasket(@PathVariable String basketReference) {
        log.info("Compensation requested for basket {}", basketReference);

        try {
            redemptionService.compensateBasketRedemption(basketReference);
            return ResponseEntity.ok("Basket redemption compensated successfully");
            
        } catch (Exception e) {
            log.error("Error compensating basket: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error compensating basket: " + e.getMessage());
        }
    }
}
