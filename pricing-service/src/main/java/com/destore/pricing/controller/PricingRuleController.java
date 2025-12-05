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
 * REST controller for pricing rule management.
 * <p>
 * This controller provides CRUD operations for pricing rules.
 * Used by Store Managers (store-specific rules) and Network Managers (global rules)
 * to configure pricing and promotions.
 * </p>
 * <p>
 * Endpoints:
 * <ul>
 *   <li>GET /api/pricing/rules - List all pricing rules</li>
 *   <li>GET /api/pricing/rules/{id} - Get specific rule</li>
 *   <li>GET /api/pricing/rules/item/{itemId} - Get rules for item</li>
 *   <li>GET /api/pricing/rules/store/{storeId} - Get rules for store</li>
 *   <li>GET /api/pricing/rules/active - Get all active rules</li>
 *   <li>POST /api/pricing/rules - Create new pricing rule</li>
 *   <li>PUT /api/pricing/rules/{id} - Update existing rule</li>
 *   <li>DELETE /api/pricing/rules/{id} - Delete rule</li>
 *   <li>POST /api/pricing/rules/{id}/deactivate - Soft delete (deactivate)</li>
 * </ul>
 * </p>
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
     * Creates a new pricing rule.
     * <p>
     * Network Managers can create global rules (is_global=true, store_id=null).
     * Store Managers can create store-specific rules (is_global=false, store_id set).
     * </p>
     * <p>
     * Validation:
     * <ul>
     *   <li>Item must exist in warehouse database</li>
     *   <li>Store must exist (if store-specific)</li>
     *   <li>Global rules cannot have store_id</li>
     *   <li>Promotion value required for PERCENTAGE_OFF and FIXED_DISCOUNT</li>
     * </ul>
     * </p>
     *
     * @param request the pricing rule creation request
     * @return the created {@link PricingRuleResponse}
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
     * Gets all pricing rules.
     *
     * @return a list of all {@link PricingRuleResponse}
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
     * Gets all active pricing rules.
     *
     * @return a list of currently active {@link PricingRuleResponse}
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
     * Gets a specific pricing rule by ID.
     *
     * @param ruleId the rule ID
     * @return the {@link PricingRuleResponse} details
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
    public ResponseEntity<PricingRuleResponse> getRule(@PathVariable Integer ruleId) {
        log.info("Fetching pricing rule: ruleId={}", ruleId);
        PricingRuleResponse response = pricingRuleService.getRule(ruleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all pricing rules for a specific item.
     *
     * @param itemId the warehouse item ID
     * @return a list of {@link PricingRuleResponse} for the item
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
     * Gets all pricing rules for a specific store.
     *
     * @param storeId the store ID
     * @return a list of store-specific {@link PricingRuleResponse}
     */
    @GetMapping("/store/{storeId}")
    @Operation(
        summary = "Get pricing rules for a store",
        description = "Retrieve all store-specific pricing rules for a particular store"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    })
    public ResponseEntity<List<PricingRuleResponse>> getRulesByStore(@PathVariable Integer storeId) {
        log.info("Fetching pricing rules for store: storeId={}", storeId);
        List<PricingRuleResponse> rules = pricingRuleService.getRulesByStore(storeId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Updates an existing pricing rule.
     *
     * @param ruleId the rule ID to update
     * @param request the updated rule data
     * @return the updated {@link PricingRuleResponse}
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
            @PathVariable Integer ruleId,
            @Valid @RequestBody PricingRuleRequest request) {
        
        log.info("Updating pricing rule: ruleId={}", ruleId);
        PricingRuleResponse response = pricingRuleService.updateRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a pricing rule (hard delete).
     *
     * @param ruleId the rule ID to delete
     * @return a {@link ResponseEntity} with no content
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
    public ResponseEntity<Void> deleteRule(@PathVariable Integer ruleId) {
        log.info("Deleting pricing rule: ruleId={}", ruleId);
        pricingRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivates a pricing rule (soft delete).
     * <p>
     * Sets is_active=false instead of deleting the record.
     * Useful for maintaining audit trail.
     * </p>
     *
     * @param ruleId the rule ID to deactivate
     * @return the deactivated {@link PricingRuleResponse}
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
    public ResponseEntity<PricingRuleResponse> deactivateRule(@PathVariable Integer ruleId) {
        log.info("Deactivating pricing rule: ruleId={}", ruleId);
        PricingRuleResponse response = pricingRuleService.deactivateRule(ruleId);
        return ResponseEntity.ok(response);
    }
}
