package com.destore.pricing.controller;

import com.destore.pricing.model.dto.PriceCalculationRequest;
import com.destore.pricing.model.dto.PriceCalculationResponse;
import com.destore.pricing.service.PriceCalculationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @file PriceCalculationController.java
 * @brief REST controller for price calculation operations
 * 
 * This controller handles price calculation requests from the Shopping System.
 * It applies hierarchical pricing rules and promotional discounts to calculate
 * final prices.
 * 
 * Endpoints:
 * - POST /api/pricing/calculate - Calculate final price with promotions
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Price Calculation", description = "Calculate final prices with promotions")
public class PriceCalculationController {

    private final PriceCalculationEngine priceCalculationEngine;

    /**
     * @brief Calculate final price for an item with quantity and promotions
     * 
     * This endpoint is called by the Shopping System during checkout to calculate
     * the final price including any applicable promotions. The calculation uses
     * hierarchical pricing where store-specific rules override global rules.
     * 
     * Request body contains:
     * - itemId: Warehouse item identifier
     * - storeId: Store identifier (optional, null = use global pricing)
     * - quantity: Number of items being purchased
     * 
     * Response contains detailed price breakdown:
     * - unitPrice: Price per item
     * - subtotal: unitPrice × quantity
     * - discountAmount: Total promotional discount
     * - finalPrice: subtotal - discountAmount
     * - promotion: Type of promotion applied
     * - usedStoreSpecificRule: Whether store-specific pricing was used
     * 
     * @param request Price calculation request
     * @return Detailed price calculation response
     */
    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate item price with promotions",
        description = "Calculate final price for an item considering quantity and applicable promotions. " +
                     "Store-specific pricing overrides global pricing when available."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Price calculated successfully",
            content = @Content(schema = @Schema(implementation = PriceCalculationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request (validation error)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Item not found or no pricing rule exists",
            content = @Content
        )
    })
    public ResponseEntity<PriceCalculationResponse> calculatePrice(
            @Valid @RequestBody PriceCalculationRequest request) {
        
        log.info("Price calculation request: itemId={}, storeId={}, quantity={}", 
                 request.getItemId(), request.getStoreId(), request.getQuantity());

        PriceCalculationResponse response = priceCalculationEngine.calculatePrice(request);

        log.info("Price calculation response: finalPrice={}, discount={}", 
                 response.getFinalPrice(), response.getDiscount());

        return ResponseEntity.ok(response);
    }

    /**
     * @brief Check if a pricing rule exists for an item/store combination
     * 
     * Utility endpoint to verify if pricing is configured before attempting
     * price calculation. Used for validation in the UI.
     * 
     * @param itemId The item ID
     * @param storeId The store ID (optional)
     * @return true if pricing rule exists
     */
    @GetMapping("/check")
    @Operation(
        summary = "Check if pricing rule exists",
        description = "Verify if an active pricing rule exists for the given item and store"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed")
    })
    public ResponseEntity<Boolean> checkPricingExists(
            @RequestParam Integer itemId,
            @RequestParam(required = false) Long storeId) {
        
        boolean exists = priceCalculationEngine.hasPricingRule(itemId, storeId);
        return ResponseEntity.ok(exists);
    }
}
