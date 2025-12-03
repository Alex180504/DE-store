package com.destore.pricing.integration;

import com.destore.pricing.model.dto.PricingRuleRequest;
import com.destore.pricing.model.dto.PricingRuleResponse;
import com.destore.pricing.model.enums.PromotionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @file PricingRuleCRUDIntegrationTest.java
 * @brief Integration tests for pricing rule CRUD operations
 * 
 * Tests the complete lifecycle of pricing rules:
 * - Create (POST /api/pricing/rules)
 * - Read (GET /api/pricing/rules, GET /api/pricing/rules/{id})
 * - Update (PUT /api/pricing/rules/{id})
 * - Delete (DELETE /api/pricing/rules/{id})
 * - Deactivate (POST /api/pricing/rules/{id}/deactivate)
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PricingRuleCRUDIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * @brief Test creating a global pricing rule
     */
    @Test
    public void testCreateGlobalRule() {
        PricingRuleRequest request = PricingRuleRequest.builder()
                .itemId(1)
                .price(new BigDecimal("29.99"))
                .promotion(PromotionType.NONE)
                .isGlobal(true)
                .createdBy("Test User")
                .build();

        ResponseEntity<PricingRuleResponse> response = restTemplate.postForEntity(
                "/api/pricing/rules",
                request,
                PricingRuleResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        PricingRuleResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getRuleId()).isNotNull();
        assertThat(result.getItemId()).isEqualTo(1);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(result.getIsGlobal()).isTrue();
        assertThat(result.getStoreId()).isNull();
        assertThat(result.getIsActive()).isTrue();
    }

    /**
     * @brief Test creating a store-specific pricing rule
     */
    @Test
    public void testCreateStoreSpecificRule() {
        PricingRuleRequest request = PricingRuleRequest.builder()
                .itemId(2)
                .storeId(1L)
                .price(new BigDecimal("24.99"))
                .promotion(PromotionType.PERCENTAGE_OFF)
                .promotionValue(new BigDecimal("10"))
                .isGlobal(false)
                .createdBy("Store Manager")
                .build();

        ResponseEntity<PricingRuleResponse> response = restTemplate.postForEntity(
                "/api/pricing/rules",
                request,
                PricingRuleResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        PricingRuleResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getStoreId()).isEqualTo(1L);
        assertThat(result.getIsGlobal()).isFalse();
        assertThat(result.getPromotion()).isEqualTo(PromotionType.PERCENTAGE_OFF);
        assertThat(result.getPromotionValue()).isEqualByComparingTo(new BigDecimal("10"));
    }

    /**
     * @brief Test creating a rule with validity dates
     */
    @Test
    public void testCreateRuleWithValidityDates() {
        LocalDateTime validFrom = LocalDateTime.now();
        LocalDateTime validTo = validFrom.plusDays(30);

        PricingRuleRequest request = PricingRuleRequest.builder()
                .itemId(3)
                .price(new BigDecimal("19.99"))
                .promotion(PromotionType.FIXED_DISCOUNT)
                .promotionValue(new BigDecimal("5"))
                .isGlobal(true)
                .validFrom(validFrom)
                .validTo(validTo)
                .createdBy("Test User")
                .build();

        ResponseEntity<PricingRuleResponse> response = restTemplate.postForEntity(
                "/api/pricing/rules",
                request,
                PricingRuleResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        PricingRuleResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getValidFrom()).isNotNull();
        assertThat(result.getValidTo()).isNotNull();
    }

    /**
     * @brief Test getting all pricing rules
     */
    @Test
    public void testGetAllRules() {
        ResponseEntity<List<PricingRuleResponse>> response = restTemplate.exchange(
                "/api/pricing/rules",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PricingRuleResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        List<PricingRuleResponse> rules = response.getBody();
        assertThat(rules).isNotNull();
        // Should have at least the sample data from init script
        assertThat(rules).isNotEmpty();
    }

    /**
     * @brief Test getting active rules only
     */
    @Test
    public void testGetActiveRules() {
        ResponseEntity<List<PricingRuleResponse>> response = restTemplate.exchange(
                "/api/pricing/rules/active",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PricingRuleResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        List<PricingRuleResponse> rules = response.getBody();
        assertThat(rules).isNotNull();
        
        // All returned rules should be active
        rules.forEach(rule -> {
            assertThat(rule.getIsActive()).isTrue();
        });
    }

    /**
     * @brief Test getting a specific rule by ID
     */
    @Test
    public void testGetRuleById() {
        // First create a rule
        PricingRuleRequest createRequest = PricingRuleRequest.builder()
                .itemId(4)
                .price(new BigDecimal("39.99"))
                .promotion(PromotionType.BOGOF)
                .isGlobal(true)
                .createdBy("Test User")
                .build();

        ResponseEntity<PricingRuleResponse> createResponse = restTemplate.postForEntity(
                "/api/pricing/rules",
                createRequest,
                PricingRuleResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long ruleId = createResponse.getBody().getRuleId();

        // Now get it by ID
        ResponseEntity<PricingRuleResponse> getResponse = restTemplate.getForEntity(
                "/api/pricing/rules/" + ruleId,
                PricingRuleResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PricingRuleResponse result = getResponse.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getRuleId()).isEqualTo(ruleId);
        assertThat(result.getItemId()).isEqualTo(4);
    }

    /**
     * @brief Test getting rules by item ID
     */
    @Test
    public void testGetRulesByItem() {
        int itemId = 5;

        // Create multiple rules for the same item
        for (int i = 0; i < 3; i++) {
            PricingRuleRequest request = PricingRuleRequest.builder()
                    .itemId(itemId)
                    .price(new BigDecimal("29.99").add(BigDecimal.valueOf(i)))
                    .promotion(PromotionType.NONE)
                    .isGlobal(i == 0) // First one global, others store-specific
                    .storeId(i > 0 ? (long) i : null)
                    .createdBy("Test User")
                    .build();

            restTemplate.postForEntity("/api/pricing/rules", request, PricingRuleResponse.class);
        }

        ResponseEntity<List<PricingRuleResponse>> response = restTemplate.exchange(
                "/api/pricing/rules/item/" + itemId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PricingRuleResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        List<PricingRuleResponse> rules = response.getBody();
        assertThat(rules).isNotNull();
        assertThat(rules).hasSizeGreaterThanOrEqualTo(3);
        
        // All should be for the same item
        rules.forEach(rule -> {
            assertThat(rule.getItemId()).isEqualTo(itemId);
        });
    }

    /**
     * @brief Test getting rules by store ID
     */
    @Test
    public void testGetRulesByStore() {
        long storeId = 2L;

        // Create rules for specific store
        for (int i = 0; i < 2; i++) {
            PricingRuleRequest request = PricingRuleRequest.builder()
                    .itemId(6 + i)
                    .storeId(storeId)
                    .price(new BigDecimal("19.99"))
                    .promotion(PromotionType.NONE)
                    .isGlobal(false)
                    .createdBy("Store Manager")
                    .build();

            restTemplate.postForEntity("/api/pricing/rules", request, PricingRuleResponse.class);
        }

        ResponseEntity<List<PricingRuleResponse>> response = restTemplate.exchange(
                "/api/pricing/rules/store/" + storeId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PricingRuleResponse>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        List<PricingRuleResponse> rules = response.getBody();
        assertThat(rules).isNotNull();
        assertThat(rules).isNotEmpty();
        
        // All should be for the same store
        rules.forEach(rule -> {
            assertThat(rule.getStoreId()).isEqualTo(storeId);
        });
    }

    /**
     * @brief Test updating a pricing rule
     */
    @Test
    public void testUpdateRule() {
        // Create initial rule
        PricingRuleRequest createRequest = PricingRuleRequest.builder()
                .itemId(7)
                .price(new BigDecimal("49.99"))
                .promotion(PromotionType.NONE)
                .isGlobal(true)
                .createdBy("Test User")
                .build();

        ResponseEntity<PricingRuleResponse> createResponse = restTemplate.postForEntity(
                "/api/pricing/rules",
                createRequest,
                PricingRuleResponse.class
        );

        Long ruleId = createResponse.getBody().getRuleId();

        // Update the rule
        PricingRuleRequest updateRequest = PricingRuleRequest.builder()
                .itemId(7)
                .price(new BigDecimal("44.99")) // Changed price
                .promotion(PromotionType.PERCENTAGE_OFF) // Changed promotion
                .promotionValue(new BigDecimal("15"))
                .isGlobal(true)
                .createdBy("Test User")
                .build();

        HttpEntity<PricingRuleRequest> httpEntity = new HttpEntity<>(updateRequest);
        
        ResponseEntity<PricingRuleResponse> updateResponse = restTemplate.exchange(
                "/api/pricing/rules/" + ruleId,
                HttpMethod.PUT,
                httpEntity,
                PricingRuleResponse.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PricingRuleResponse result = updateResponse.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getRuleId()).isEqualTo(ruleId);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("44.99"));
        assertThat(result.getPromotion()).isEqualTo(PromotionType.PERCENTAGE_OFF);
    }

    /**
     * @brief Test deactivating a pricing rule (soft delete)
     */
    @Test
    public void testDeactivateRule() {
        // Create a rule
        PricingRuleRequest createRequest = PricingRuleRequest.builder()
                .itemId(8)
                .price(new BigDecimal("34.99"))
                .promotion(PromotionType.THREE_FOR_TWO)
                .isGlobal(true)
                .createdBy("Test User")
                .build();

        ResponseEntity<PricingRuleResponse> createResponse = restTemplate.postForEntity(
                "/api/pricing/rules",
                createRequest,
                PricingRuleResponse.class
        );

        Long ruleId = createResponse.getBody().getRuleId();

        // Deactivate it
        ResponseEntity<PricingRuleResponse> deactivateResponse = restTemplate.postForEntity(
                "/api/pricing/rules/" + ruleId + "/deactivate",
                null,
                PricingRuleResponse.class
        );

        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PricingRuleResponse result = deactivateResponse.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isFalse();

        // Verify it doesn't appear in active rules
        ResponseEntity<List<PricingRuleResponse>> activeRulesResponse = restTemplate.exchange(
                "/api/pricing/rules/active",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PricingRuleResponse>>() {}
        );

        List<PricingRuleResponse> activeRules = activeRulesResponse.getBody();
        assertThat(activeRules).isNotNull();
        assertThat(activeRules).noneMatch(rule -> rule.getRuleId().equals(ruleId));
    }

    /**
     * @brief Test deleting a pricing rule (hard delete)
     */
    @Test
    public void testDeleteRule() {
        // Create a rule
        PricingRuleRequest createRequest = PricingRuleRequest.builder()
                .itemId(9)
                .price(new BigDecimal("54.99"))
                .promotion(PromotionType.NONE)
                .isGlobal(true)
                .createdBy("Test User")
                .build();

        ResponseEntity<PricingRuleResponse> createResponse = restTemplate.postForEntity(
                "/api/pricing/rules",
                createRequest,
                PricingRuleResponse.class
        );

        Long ruleId = createResponse.getBody().getRuleId();

        // Delete it
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/pricing/rules/" + ruleId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it no longer exists
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/pricing/rules/" + ruleId,
                String.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * @brief Test validation error when creating rule with missing required fields
     */
    @Test
    public void testCreateRule_ValidationError() {
        PricingRuleRequest request = PricingRuleRequest.builder()
                .itemId(null) // Missing required field
                .price(new BigDecimal("29.99"))
                .promotion(PromotionType.NONE)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/pricing/rules",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Test error when creating rule for non-existent item
     */
    @Test
    public void testCreateRule_ItemNotFound() {
        PricingRuleRequest request = PricingRuleRequest.builder()
                .itemId(9999) // Non-existent item
                .price(new BigDecimal("29.99"))
                .promotion(PromotionType.NONE)
                .isGlobal(true)
                .createdBy("Test User")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/pricing/rules",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
