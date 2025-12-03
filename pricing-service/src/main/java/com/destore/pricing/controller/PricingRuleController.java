package com.destore.pricing.controller;

import com.destore.pricing.model.dto.PricingRuleRequest;
import com.destore.pricing.model.dto.PricingRuleResponse;
import com.destore.pricing.service.PricingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @file PricingRuleController.java
 * @brief REST controller for pricing rule management
 * 
 * This controller provides CRUD operations for pricing rules.
 * Used by Store Managers (store-specific rules) and Network Managers (global rules)
 * to configure pricing and promotions.
 * 
 * Endpoints:
 * - GET /api/pricing/rules - List all pricing rules
 * - GET /api/pricing/rules/{id} - Get specific rule
 * - GET /api/pricing/rules/item/{itemId} - Get rules for item
 * - GET /api/pricing/rules/store/{storeId} - Get rules for store
 * - GET /api/pricing/rules/active - Get all active rules
 * - POST /api/pricing/rules - Create new pricing rule
 * - PUT /api/pricing/rules/{id} - Update existing rule
 * - DELETE /api/pricing/rules/{id} - Delete rule
 * - POST /api/pricing/rules/{id}/deactivate - Soft delete (deactivate)
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/pricing/rules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pricing Rules", description = "Manage pricing rules and promotions")
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;

    /**
     * @brief Create a new pricing rule
     * 
     * Network Managers can create global rules (is_global=true, store_id=null).
     * Store Managers can create store-specific rules (is_global=false, store_id set).
     * 
     * Validation:
     * - Item must exist in warehouse database
     * - Store must exist (if store-specific)
     * - Global rules cannot have store_id
     * - Promotion value required for PERCENTAGE_OFF and FIXED_DISCOUNT
     * 
     * @param request Pricing rule creation request
     * @return Created pricing rule
     */
    @PostMapping
    @Operation(
        summary = "Create new pricing rule",
        description = "Create a global or store-specific pricing rule with optional promotions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Pricing rule created successfully",
            content = @Content(schema = @Schema(implementation = PricingRuleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Item or store not found",
            content = @Content
        )
    })
    public ResponseEntity<PricingRuleResponse> createRule(@Valid @RequestBody PricingRuleRequest request) {
        log.info("Creating pricing rule: itemId={}, storeId={}, isGlobal={}", 
                 request.getItemId(), request.getStoreId(), request.getIsGlobal());
        
        PricingRuleResponse response = pricingRuleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * @brief Get all pricing rules
     * @return List of all pricing rules
     */
    @GetMapping
    @Operation(
        summary = "List all pricing rules",
        description = "Retrieve all pricing rules (active and inactive)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    })
    public ResponseEntity<List<PricingRuleResponse>> getAllRules() {
        log.info("Fetching all pricing rules");
        List<PricingRuleResponse> rules = pricingRuleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * @brief Get all active pricing rules
     * @return List of currently active rules
     */
    @GetMapping("/active")
    @Operation(
        summary = "List active pricing rules",
        description = "Retrieve only currently active pricing rules within their validity period"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active rules retrieved successfully")
    })
    public ResponseEntity<List<PricingRuleResponse>> getActiveRules() {
        log.info("Fetching active pricing rules");
        List<PricingRuleResponse> rules = pricingRuleService.getActiveRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * @brief Get a specific pricing rule by ID
     * @param ruleId The rule ID
     * @return Pricing rule details
     */
    @GetMapping("/{ruleId}")
    @Operation(
        summary = "Get pricing rule by ID",
        description = "Retrieve detailed information about a specific pricing rule"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rule retrieved successfully",
            content = @Content(schema = @Schema(implementation = PricingRuleResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pricing rule not found",
            content = @Content
        )
    })
    public ResponseEntity<PricingRuleResponse> getRule(@PathVariable Long ruleId) {
        log.info("Fetching pricing rule: ruleId={}", ruleId);
        PricingRuleResponse response = pricingRuleService.getRule(ruleId);
        return ResponseEntity.ok(response);
    }

    /**
     * @brief Get all pricing rules for a specific item
     * @param itemId The warehouse item ID
     * @return List of rules for the item
     */
    @GetMapping("/item/{itemId}")
    @Operation(
        summary = "Get pricing rules for an item",
        description = "Retrieve all pricing rules (global and store-specific) for a warehouse item"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    })
    public ResponseEntity<List<PricingRuleResponse>> getRulesByItem(@PathVariable Integer itemId) {
        log.info("Fetching pricing rules for item: itemId={}", itemId);
        List<PricingRuleResponse> rules = pricingRuleService.getRulesByItem(itemId);
        return ResponseEntity.ok(rules);
    }

    /**
     * @brief Get all pricing rules for a specific store
     * @param storeId The store ID
     * @return List of store-specific rules
     */
    @GetMapping("/store/{storeId}")
    @Operation(
        summary = "Get pricing rules for a store",
        description = "Retrieve all store-specific pricing rules for a particular store"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    })
    public ResponseEntity<List<PricingRuleResponse>> getRulesByStore(@PathVariable Long storeId) {
        log.info("Fetching pricing rules for store: storeId={}", storeId);
        List<PricingRuleResponse> rules = pricingRuleService.getRulesByStore(storeId);
        return ResponseEntity.ok(rules);
    }

    /**
     * @brief Update an existing pricing rule
     * @param ruleId The rule ID to update
     * @param request Updated rule data
     * @return Updated pricing rule
     */
    @PutMapping("/{ruleId}")
    @Operation(
        summary = "Update pricing rule",
        description = "Modify an existing pricing rule (price, promotion, validity dates)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rule updated successfully",
            content = @Content(schema = @Schema(implementation = PricingRuleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pricing rule not found",
            content = @Content
        )
    })
    public ResponseEntity<PricingRuleResponse> updateRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody PricingRuleRequest request) {
        
        log.info("Updating pricing rule: ruleId={}", ruleId);
        PricingRuleResponse response = pricingRuleService.updateRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * @brief Delete a pricing rule (hard delete)
     * @param ruleId The rule ID to delete
     * @return No content response
     */
    @DeleteMapping("/{ruleId}")
    @Operation(
        summary = "Delete pricing rule",
        description = "Permanently delete a pricing rule from the database"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "Pricing rule not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
        log.info("Deleting pricing rule: ruleId={}", ruleId);
        pricingRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * @brief Deactivate a pricing rule (soft delete)
     * 
     * Sets is_active=false instead of deleting the record.
     * Useful for maintaining audit trail.
     * 
     * @param ruleId The rule ID to deactivate
     * @return Deactivated pricing rule
     */
    @PostMapping("/{ruleId}/deactivate")
    @Operation(
        summary = "Deactivate pricing rule",
        description = "Soft delete a pricing rule by setting is_active=false (maintains audit trail)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rule deactivated successfully",
            content = @Content(schema = @Schema(implementation = PricingRuleResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pricing rule not found",
            content = @Content
        )
    })
    public ResponseEntity<PricingRuleResponse> deactivateRule(@PathVariable Long ruleId) {
        log.info("Deactivating pricing rule: ruleId={}", ruleId);
        PricingRuleResponse response = pricingRuleService.deactivateRule(ruleId);
        return ResponseEntity.ok(response);
    }
}
