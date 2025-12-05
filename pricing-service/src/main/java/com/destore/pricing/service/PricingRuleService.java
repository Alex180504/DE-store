package com.destore.pricing.service;

import com.destore.pricing.exception.ForbiddenAccessException;
import com.destore.pricing.exception.ItemNotFoundException;
import com.destore.pricing.exception.PricingRuleNotFoundException;
import com.destore.pricing.model.dto.PricingRuleRequest;
import com.destore.pricing.model.dto.PricingRuleResponse;
import com.destore.pricing.model.entity.PricingRule;
import com.destore.pricing.repository.PricingRuleRepository;
import com.destore.pricing.repository.WarehouseItemRepository;
import com.destore.pricing.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing pricing rules (CRUD operations).
 * <p>
 * This service provides business logic for creating, reading, updating,
 * and deleting pricing rules. It enforces validation rules and converts
 * between entities and DTOs.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validate item exists in warehouse before creating rules</li>
 *   <li>Validate store exists before creating store-specific rules</li>
 *   <li>Convert between PricingRule entities and DTOs</li>
 *   <li>Manage rule lifecycle (create, update, delete)</li>
 * </ul>
 * </p>
 *
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final WarehouseItemRepository warehouseItemRepository;

    /**
     * Creates a new pricing rule.
     * <p>
     * Validation:
     * <ol>
     *   <li>Verify item exists in warehouse database</li>
     *   <li>If store-specific rule, verify store exists</li>
     *   <li>Ensure global rules don't have store_id</li>
     * </ol>
     * </p>
     *
     * @param request the pricing rule request
     * @return the created {@link PricingRuleResponse}
     * @throws ItemNotFoundException if item doesn't exist
     */
    @Transactional
    public PricingRuleResponse createRule(PricingRuleRequest request) {
        log.info("Creating pricing rule: itemId={}, storeId={}, isGlobal={}", 
                 request.getItemId(), request.getStoreId(), request.getIsGlobal());

        // Authorization check
        UserPrincipal currentUser = getCurrentUser();
        validateCreatePermission(currentUser, request);

        // Validate item exists
        if (!warehouseItemRepository.existsById(request.getItemId())) {
            throw new ItemNotFoundException(request.getItemId());
        }

        // Build entity
        PricingRule rule = PricingRule.builder()
                .itemId(request.getItemId())
                .storeId(request.getStoreId()) // Store ID references store-service
                .price(request.getPrice())
                .promotion(request.getPromotion())
                .promotionValue(request.getPromotionValue())
                .isGlobal(request.getIsGlobal())
                .validFrom(request.getValidFrom() != null ? request.getValidFrom() : LocalDateTime.now())
                .validTo(request.getValidTo())
                .isActive(true)
                .createdBy(request.getCreatedBy())
                .build();

        PricingRule saved = pricingRuleRepository.save(rule);
        log.info("Pricing rule created: ruleId={}", saved.getRuleId());

        return toResponse(saved);
    }

    /**
     * Gets a pricing rule by ID.
     *
     * @param ruleId the rule ID
     * @return the {@link PricingRuleResponse}
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional(readOnly = true)
    public PricingRuleResponse getRule(Integer ruleId) {
        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));
        return toResponse(rule);
    }

    /**
     * Gets all pricing rules.
     *
     * @return a list of all {@link PricingRuleResponse}
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getAllRules() {
        return pricingRuleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets all active pricing rules.
     *
     * @return a list of currently active {@link PricingRuleResponse}
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getActiveRules() {
        return pricingRuleRepository.findAllActiveRules(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets pricing rules for a specific item.
     *
     * @param itemId the item ID
     * @return a list of {@link PricingRuleResponse} for the item
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getRulesByItem(Integer itemId) {
        return pricingRuleRepository.findByItemId(itemId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets pricing rules for a specific store.
     *
     * @param storeId the store ID
     * @return a list of store-specific {@link PricingRuleResponse}
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getRulesByStore(Integer storeId) {
        return pricingRuleRepository.findByStoreId(storeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing pricing rule.
     *
     * @param ruleId the rule ID to update
     * @param request the updated rule data
     * @return the updated {@link PricingRuleResponse}
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional
    public PricingRuleResponse updateRule(Integer ruleId, PricingRuleRequest request) {
        log.info("Updating pricing rule: ruleId={}", ruleId);

        PricingRule existing = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));

        // Authorization check
        UserPrincipal currentUser = getCurrentUser();
        validateModifyPermission(currentUser, existing);

        // Update fields
        existing.setPrice(request.getPrice());
        existing.setPromotion(request.getPromotion());
        existing.setPromotionValue(request.getPromotionValue());
        existing.setStoreId(request.getStoreId()); // Store ID references store-service
        
        // Only update validity dates if provided
        if (request.getValidFrom() != null) {
            existing.setValidFrom(request.getValidFrom());
        }
        if (request.getValidTo() != null) {
            existing.setValidTo(request.getValidTo());
        }

        PricingRule updated = pricingRuleRepository.save(existing);
        log.info("Pricing rule updated: ruleId={}", ruleId);

        return toResponse(updated);
    }

    /**
     * Deletes a pricing rule.
     *
     * @param ruleId the rule ID to delete
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional
    public void deleteRule(Integer ruleId) {
        log.info("Deleting pricing rule: ruleId={}", ruleId);

        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));

        // Authorization check
        UserPrincipal currentUser = getCurrentUser();
        validateModifyPermission(currentUser, rule);

        pricingRuleRepository.delete(rule);
        log.info("Pricing rule deleted: ruleId={}", ruleId);
    }

    /**
     * Soft deletes a pricing rule (set isActive = false).
     *
     * @param ruleId the rule ID to deactivate
     * @return the deactivated {@link PricingRuleResponse}
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional
    public PricingRuleResponse deactivateRule(Integer ruleId) {
        log.info("Deactivating pricing rule: ruleId={}", ruleId);

        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));

        // Authorization check
        UserPrincipal currentUser = getCurrentUser();
        validateModifyPermission(currentUser, rule);

        rule.setIsActive(false);
        PricingRule updated = pricingRuleRepository.save(rule);

        log.info("Pricing rule deactivated: ruleId={}", ruleId);
        return toResponse(updated);
    }

    /**
     * Gets the current authenticated user.
     *
     * @return the {@link UserPrincipal} with user info and role
     */
    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new ForbiddenAccessException("User not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    /**
     * Validates that the user has permission to create a pricing rule.
     * <p>
     * Rules:
     * <ul>
     *   <li>NETWORK_MANAGER: Can create global rules and any store-specific rules</li>
     *   <li>STORE_MANAGER: Can only create rules for their assigned store (not global)</li>
     * </ul>
     * </p>
     *
     * @param user the current authenticated user
     * @param request the pricing rule creation request
     * @throws ForbiddenAccessException if user lacks permission
     */
    private void validateCreatePermission(UserPrincipal user, PricingRuleRequest request) {
        if (user.isNetworkManager()) {
            // Network managers can create any rule
            return;
        }
        
        if (user.isStoreManager()) {
            // Store managers cannot create global rules
            if (Boolean.TRUE.equals(request.getIsGlobal())) {
                throw new ForbiddenAccessException(
                    "create global pricing rule",
                    "Store managers can only create store-specific rules"
                );
            }
            
            // Store managers can only create rules for their own store
            if (request.getStoreId() == null || !request.getStoreId().equals(user.getStoreId())) {
                throw new ForbiddenAccessException(
                    "create pricing rule for another store",
                    String.format("Store managers can only create rules for their assigned store (ID: %d)", 
                                  user.getStoreId())
                );
            }
            return;
        }
        
        throw new ForbiddenAccessException("Unknown role: " + user.getRole());
    }

    /**
     * Validates that the user has permission to modify/delete a pricing rule.
     * <p>
     * Rules:
     * <ul>
     *   <li>NETWORK_MANAGER: Can modify/delete any rule (global or store-specific)</li>
     *   <li>STORE_MANAGER: Can only modify/delete rules for their assigned store (not global)</li>
     * </ul>
     * </p>
     *
     * @param user the current authenticated user
     * @param rule the existing pricing rule to modify
     * @throws ForbiddenAccessException if user lacks permission
     */
    private void validateModifyPermission(UserPrincipal user, PricingRule rule) {
        if (user.isNetworkManager()) {
            // Network managers can modify any rule
            return;
        }
        
        if (user.isStoreManager()) {
            // Store managers cannot modify global rules
            if (Boolean.TRUE.equals(rule.getIsGlobal())) {
                throw new ForbiddenAccessException(
                    "modify global pricing rule",
                    "Store managers cannot modify global rules set by network managers"
                );
            }
            
            // Store managers can only modify rules for their own store
            Integer ruleStoreId = rule.getStoreId();
            if (ruleStoreId == null || !ruleStoreId.equals(user.getStoreId())) {
                throw new ForbiddenAccessException(
                    "modify pricing rule from another store",
                    String.format("Store managers can only modify rules for their assigned store (ID: %d)", 
                                  user.getStoreId())
                );
            }
            return;
        }
        
        throw new ForbiddenAccessException("Unknown role: " + user.getRole());
    }

    /**
     * Converts a PricingRule entity to a DTO.
     *
     * @param rule the pricing rule entity
     * @return the {@link PricingRuleResponse} DTO
     */
    private PricingRuleResponse toResponse(PricingRule rule) {
        return PricingRuleResponse.builder()
                .ruleId(rule.getRuleId())
                .itemId(rule.getItemId())
                .storeId(rule.getStoreId())
                .price(rule.getPrice())
                .promotion(rule.getPromotion())
                .promotionValue(rule.getPromotionValue())
                .isGlobal(rule.getIsGlobal())
                .validFrom(rule.getValidFrom())
                .validTo(rule.getValidTo())
                .isActive(rule.getIsActive())
                .createdBy(rule.getCreatedBy())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
