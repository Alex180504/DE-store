package com.destore.pricing.service;

import com.destore.pricing.exception.ItemNotFoundException;
import com.destore.pricing.exception.PricingRuleNotFoundException;
import com.destore.pricing.model.dto.PricingRuleRequest;
import com.destore.pricing.model.dto.PricingRuleResponse;
import com.destore.pricing.model.entity.PricingRule;
import com.destore.pricing.model.entity.Store;
import com.destore.pricing.repository.PricingRuleRepository;
import com.destore.pricing.repository.StoreRepository;
import com.destore.pricing.repository.WarehouseItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @file PricingRuleService.java
 * @brief Service for managing pricing rules (CRUD operations)
 * 
 * This service provides business logic for creating, reading, updating,
 * and deleting pricing rules. It enforces validation rules and converts
 * between entities and DTOs.
 * 
 * Responsibilities:
 * - Validate item exists in warehouse before creating rules
 * - Validate store exists before creating store-specific rules
 * - Convert between PricingRule entities and DTOs
 * - Manage rule lifecycle (create, update, delete)
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final StoreRepository storeRepository;
    private final WarehouseItemRepository warehouseItemRepository;

    /**
     * @brief Create a new pricing rule
     * 
     * Validation:
     * 1. Verify item exists in warehouse database
     * 2. If store-specific rule, verify store exists
     * 3. Ensure global rules don't have store_id
     * 
     * @param request The pricing rule request
     * @return Created pricing rule response
     * @throws ItemNotFoundException if item doesn't exist
     */
    @Transactional
    public PricingRuleResponse createRule(PricingRuleRequest request) {
        log.info("Creating pricing rule: itemId={}, storeId={}, isGlobal={}", 
                 request.getItemId(), request.getStoreId(), request.getIsGlobal());

        // Validate item exists
        if (!warehouseItemRepository.existsById(request.getItemId())) {
            throw new ItemNotFoundException(request.getItemId());
        }

        // Build entity
        PricingRule rule = PricingRule.builder()
                .itemId(request.getItemId())
                .price(request.getPrice())
                .promotion(request.getPromotion())
                .promotionValue(request.getPromotionValue())
                .isGlobal(request.getIsGlobal())
                .validFrom(request.getValidFrom() != null ? request.getValidFrom() : LocalDateTime.now())
                .validTo(request.getValidTo())
                .isActive(true)
                .createdBy(request.getCreatedBy())
                .build();

        // Set store if store-specific
        if (!Boolean.TRUE.equals(request.getIsGlobal()) && request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found: " + request.getStoreId()));
            rule.setStore(store);
        }

        PricingRule saved = pricingRuleRepository.save(rule);
        log.info("Pricing rule created: ruleId={}", saved.getRuleId());

        return toResponse(saved);
    }

    /**
     * @brief Get a pricing rule by ID
     * @param ruleId The rule ID
     * @return Pricing rule response
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional(readOnly = true)
    public PricingRuleResponse getRule(Integer ruleId) {
        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));
        return toResponse(rule);
    }

    /**
     * @brief Get all pricing rules
     * @return List of all pricing rules
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getAllRules() {
        return pricingRuleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * @brief Get all active pricing rules
     * @return List of currently active rules
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getActiveRules() {
        return pricingRuleRepository.findAllActiveRules(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * @brief Get pricing rules for a specific item
     * @param itemId The item ID
     * @return List of rules for the item
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getRulesByItem(Integer itemId) {
        return pricingRuleRepository.findByItemId(itemId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * @brief Get pricing rules for a specific store
     * @param storeId The store ID
     * @return List of store-specific rules
     */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getRulesByStore(Integer storeId) {
        return pricingRuleRepository.findByStoreId(storeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * @brief Update an existing pricing rule
     * @param ruleId The rule ID to update
     * @param request The updated rule data
     * @return Updated pricing rule response
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional
    public PricingRuleResponse updateRule(Integer ruleId, PricingRuleRequest request) {
        log.info("Updating pricing rule: ruleId={}", ruleId);

        PricingRule existing = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));

        // Update fields
        existing.setPrice(request.getPrice());
        existing.setPromotion(request.getPromotion());
        existing.setPromotionValue(request.getPromotionValue());
        
        // Only update validity dates if provided
        if (request.getValidFrom() != null) {
            existing.setValidFrom(request.getValidFrom());
        }
        if (request.getValidTo() != null) {
            existing.setValidTo(request.getValidTo());
        }

        // Update store if changed
        if (!Boolean.TRUE.equals(request.getIsGlobal()) && request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found: " + request.getStoreId()));
            existing.setStore(store);
        } else {
            existing.setStore(null);
        }

        PricingRule updated = pricingRuleRepository.save(existing);
        log.info("Pricing rule updated: ruleId={}", ruleId);

        return toResponse(updated);
    }

    /**
     * @brief Delete a pricing rule
     * @param ruleId The rule ID to delete
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional
    public void deleteRule(Integer ruleId) {
        log.info("Deleting pricing rule: ruleId={}", ruleId);

        if (!pricingRuleRepository.existsById(ruleId)) {
            throw new PricingRuleNotFoundException(ruleId);
        }

        pricingRuleRepository.deleteById(ruleId);
        log.info("Pricing rule deleted: ruleId={}", ruleId);
    }

    /**
     * @brief Soft delete a pricing rule (set isActive = false)
     * @param ruleId The rule ID to deactivate
     * @return Deactivated pricing rule response
     * @throws PricingRuleNotFoundException if rule not found
     */
    @Transactional
    public PricingRuleResponse deactivateRule(Integer ruleId) {
        log.info("Deactivating pricing rule: ruleId={}", ruleId);

        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PricingRuleNotFoundException(ruleId));

        rule.setIsActive(false);
        PricingRule updated = pricingRuleRepository.save(rule);

        log.info("Pricing rule deactivated: ruleId={}", ruleId);
        return toResponse(updated);
    }

    /**
     * @brief Convert PricingRule entity to DTO
     * @param rule The pricing rule entity
     * @return Pricing rule response DTO
     */
    private PricingRuleResponse toResponse(PricingRule rule) {
        return PricingRuleResponse.builder()
                .ruleId(rule.getRuleId())
                .itemId(rule.getItemId())
                .storeId(rule.getStore() != null ? rule.getStore().getStoreId() : null)
                .storeCode(rule.getStore() != null ? rule.getStore().getStoreCode() : null)
                .storeName(rule.getStore() != null ? rule.getStore().getStoreName() : null)
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
