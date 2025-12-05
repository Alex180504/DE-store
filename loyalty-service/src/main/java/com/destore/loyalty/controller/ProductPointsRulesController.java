package com.destore.loyalty.controller;

import com.destore.loyalty.dto.ProductPointsRuleRequest;
import com.destore.loyalty.dto.ProductPointsRuleResponse;
import com.destore.loyalty.entity.ProductPointsRule;
import com.destore.loyalty.repository.ProductPointsRuleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for product points rules CRUD operations.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/loyalty/rules")
@RequiredArgsConstructor
public class ProductPointsRulesController {

    private final ProductPointsRuleRepository ruleRepository;

    /**
     * Get all product points rules.
     *
     * @return List of all rules
     */
    @GetMapping
    public ResponseEntity<List<ProductPointsRuleResponse>> getAllRules() {
        log.info("Fetching all product points rules");
        List<ProductPointsRule> rules = ruleRepository.findAll();
        List<ProductPointsRuleResponse> responses = rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get active product points rules (non-deleted, includes inactive for UI display).
     *
     * @return List of non-deleted rules
     */
    @GetMapping("/active")
    public ResponseEntity<List<ProductPointsRuleResponse>> getActiveRules() {
        log.info("Fetching non-deleted product points rules");
        List<ProductPointsRule> rules = ruleRepository.findByIsDeletedFalse();
        List<ProductPointsRuleResponse> responses = rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific product points rule by ID.
     *
     * @param id Rule ID
     * @return Rule details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductPointsRuleResponse> getRuleById(@PathVariable Integer id) {
        log.info("Fetching product points rule with ID: {}", id);
        return ruleRepository.findById(id)
                .map(rule -> ResponseEntity.ok(mapToDTO(rule)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new product points rule.
     *
     * @param request Rule creation request
     * @return Created rule
     */
    @PostMapping
    public ResponseEntity<ProductPointsRuleResponse> createRule(@Valid @RequestBody ProductPointsRuleRequest request) {
        log.info("Creating new product points rule for item ID: {}", request.getItemId());

        ProductPointsRule rule = ProductPointsRule.builder()
                .itemId(request.getItemId())
                .pointsPerUnit(request.getPointsPerUnit())
                .pointsPerPound(request.getPointsPerPound())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .isActive(request.getIsActive())
                .isDeleted(request.getIsDeleted() != null ? request.getIsDeleted() : false)
                .build();

        ProductPointsRule saved = ruleRepository.save(rule);
        log.info("Created product points rule with ID: {}", saved.getRuleId());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    /**
     * Update an existing product points rule.
     *
     * @param id      Rule ID
     * @param request Rule update request
     * @return Updated rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductPointsRuleResponse> updateRule(
            @PathVariable Integer id,
            @Valid @RequestBody ProductPointsRuleRequest request) {
        log.info("Updating product points rule with ID: {}", id);

        return ruleRepository.findById(id)
                .map(existing -> {
                    existing.setItemId(request.getItemId());
                    existing.setPointsPerUnit(request.getPointsPerUnit());
                    existing.setPointsPerPound(request.getPointsPerPound());
                    existing.setValidFrom(request.getValidFrom());
                    existing.setValidTo(request.getValidTo());
                    existing.setIsActive(request.getIsActive());
                    if (request.getIsDeleted() != null) {
                        existing.setIsDeleted(request.getIsDeleted());
                    }
                    existing.setUpdatedAt(LocalDateTime.now());

                    ProductPointsRule updated = ruleRepository.save(existing);
                    log.info("Updated product points rule with ID: {}", id);

                    return ResponseEntity.ok(mapToDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete (deactivate) a product points rule.
     * <p>
     * Rules are never truly deleted to preserve historical calculation accuracy.
     * This endpoint deactivates the rule and sets deactivatedAt timestamp.
     * </p>
     *
     * @param id Rule ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Integer id) {
        log.info("Deactivating product points rule with ID: {}", id);

        return ruleRepository.findById(id)
                .map(rule -> {
                    rule.setIsActive(false);
                    rule.setIsDeleted(true);
                    rule.setDeactivatedAt(LocalDateTime.now());
                    rule.setUpdatedAt(LocalDateTime.now());
                    ruleRepository.save(rule);
                    
                    log.info("Deactivated product points rule with ID: {}", id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Toggle rule active status.
     *
     * @param id Rule ID
     * @return Updated rule
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ProductPointsRuleResponse> toggleRuleStatus(@PathVariable Integer id) {
        log.info("Toggling status for product points rule with ID: {}", id);

        return ruleRepository.findById(id)
                .map(rule -> {
                    rule.setIsActive(!rule.getIsActive());
                    rule.setUpdatedAt(LocalDateTime.now());
                    ProductPointsRule updated = ruleRepository.save(rule);
                    log.info("Toggled rule status to: {}", updated.getIsActive());
                    return ResponseEntity.ok(mapToDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Maps entity to DTO.
     *
     * @param rule Product points rule entity
     * @return Product points rule response DTO
     */
    private ProductPointsRuleResponse mapToDTO(ProductPointsRule rule) {
        return ProductPointsRuleResponse.builder()
                .ruleId(rule.getRuleId())
                .itemId(rule.getItemId())
                .pointsPerUnit(rule.getPointsPerUnit())
                .pointsPerPound(rule.getPointsPerPound())
                .validFrom(rule.getValidFrom())
                .validTo(rule.getValidTo())
                .isActive(rule.getIsActive())
                .isDeleted(rule.getIsDeleted())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .deactivatedAt(rule.getDeactivatedAt())
                .build();
    }
}
