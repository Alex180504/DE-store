package com.destore.pricing.integration;

import com.destore.pricing.model.entity.PricingRule;
import com.destore.pricing.model.dto.PriceCalculationRequest;
import com.destore.pricing.model.dto.PriceCalculationResponse;
import com.destore.pricing.model.enums.PromotionType;
import com.destore.pricing.repository.PricingRuleRepository;
import com.destore.pricing.repository.WarehouseItemRepository;
import com.destore.pricing.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @file PriceCalculationIntegrationTest.java
 * @brief Integration tests for price calculation endpoint
 * 
 * Tests the complete price calculation workflow including:
 * - Hierarchical rule selection (store-specific overrides global)
 * - All promotion type calculations
 * - Multiple quantity scenarios
 * - Error handling for invalid inputs
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PriceCalculationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private WarehouseItemRepository warehouseItemRepository;

    private String testToken = "test-token";

    @BeforeEach
    void setUp() {
        pricingRuleRepository.deleteAll();

        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(jwtUtil.extractRole(anyString())).thenReturn("ROLE_USER");

        // Mock warehouse repository to always return true for item existence
        when(warehouseItemRepository.existsById(anyInt())).thenReturn(true);
        // Mock base price if needed, though logic might not use it directly if not calculating from base
        when(warehouseItemRepository.getItemBasePrice(anyInt())).thenReturn(Optional.of(new BigDecimal("100.00")));

        createTestData();
    }

    private void createTestData() {
        LocalDateTime now = LocalDateTime.now();
        
        // Item 1: Basic Price, No Promotion
        createRule(1, null, new BigDecimal("10.00"), PromotionType.NONE, null, true);

        // Item 2: 3 For 2
        createRule(2, null, new BigDecimal("10.00"), PromotionType.THREE_FOR_TWO, null, true);

        // Item 3: BOGOF
        createRule(3, null, new BigDecimal("10.00"), PromotionType.BOGOF, null, true);

        // Item 4: 10% Off
        createRule(4, null, new BigDecimal("10.00"), PromotionType.PERCENTAGE_OFF, new BigDecimal("10.00"), true);

        // Item 5: Fixed Discount 5.00
        createRule(5, null, new BigDecimal("10.00"), PromotionType.FIXED_DISCOUNT, new BigDecimal("5.00"), true);

        // Item 10: Hierarchical Pricing
        // Global Rule
        createRule(10, null, new BigDecimal("20.00"), PromotionType.NONE, null, true);
        // Store 1 Rule (London)
        createRule(10, 1, new BigDecimal("15.00"), PromotionType.NONE, null, false);
    }

    private void createRule(Integer itemId, Integer storeId, BigDecimal price, PromotionType promotion, BigDecimal promotionValue, boolean isGlobal) {
        PricingRule rule = new PricingRule();
        rule.setItemId(itemId);
        rule.setStoreId(storeId);
        rule.setPrice(price);
        rule.setPromotion(promotion);
        rule.setPromotionValue(promotionValue);
        rule.setIsGlobal(isGlobal);
        rule.setIsActive(true);
        rule.setValidFrom(LocalDateTime.now().minusDays(1));
        rule.setValidTo(LocalDateTime.now().plusDays(1));
        rule.setCreatedAt(LocalDateTime.now());
        rule.setCreatedBy("test-setup");
        pricingRuleRepository.save(rule);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testToken);
        return headers;
    }

    /**
     * @brief Test basic price calculation with no promotion
     */
    @Test
    public void testBasicPriceCalculation_NoPromotion() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(1)
                .storeId(1)
                .quantity(2)
                .build();

        ResponseEntity<PriceCalculationResponse> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PriceCalculationResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(1);
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getPromotionApplied()).isEqualTo(PromotionType.NONE);
        assertThat(result.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * @brief Test THREE_FOR_TWO promotion calculation
     * Buy 3, pay for 2 (1 free item)
     */
    @Test
    public void testPriceCalculation_ThreeForTwo() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(2) // Assuming item 2 has 3FOR2 promotion
                .storeId(1)
                .quantity(6) // Should get 2 free items
                .build();

        ResponseEntity<PriceCalculationResponse> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PriceCalculationResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getPromotionApplied()).isEqualTo(PromotionType.THREE_FOR_TWO);
        
        // Calculate expected discount: 2 free items * unit price
        BigDecimal expectedDiscount = result.getUnitPrice().multiply(new BigDecimal("2"));
        assertThat(result.getDiscount()).isEqualByComparingTo(expectedDiscount);
    }

    /**
     * @brief Test BOGOF (Buy One Get One Free) promotion
     * Buy 2, pay for 1
     */
    @Test
    public void testPriceCalculation_BOGOF() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(3) // Assuming item 3 has BOGOF
                .storeId(1)
                .quantity(4) // Should get 2 free
                .build();

        ResponseEntity<PriceCalculationResponse> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PriceCalculationResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getPromotionApplied()).isEqualTo(PromotionType.BOGOF);
        
        // Expected: pay for 2, get 2 free
        BigDecimal expectedDiscount = result.getUnitPrice().multiply(new BigDecimal("2"));
        assertThat(result.getDiscount()).isEqualByComparingTo(expectedDiscount);
    }

    /**
     * @brief Test percentage discount promotion
     * 10% off the subtotal
     */
    @Test
    public void testPriceCalculation_PercentageOff() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(4) // Assuming item 4 has 10% off
                .storeId(1)
                .quantity(5)
                .build();

        ResponseEntity<PriceCalculationResponse> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PriceCalculationResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getPromotionApplied()).isEqualTo(PromotionType.PERCENTAGE_OFF);
        
        // Discount should be (subtotal * percentage / 100)
        BigDecimal expectedDiscount = result.getSubtotal()
                .multiply(result.getPromotionValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP); 
        
        assertThat(result.getDiscount()).isEqualByComparingTo(expectedDiscount);
    }

    /**
     * @brief Test fixed discount promotion
     * £5 off the order
     */
    @Test
    public void testPriceCalculation_FixedDiscount() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(5) // Assuming item 5 has £5 off
                .storeId(1)
                .quantity(3)
                .build();

        ResponseEntity<PriceCalculationResponse> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PriceCalculationResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getPromotionApplied()).isEqualTo(PromotionType.FIXED_DISCOUNT);
        
        // Discount is the minimum of fixed amount or subtotal
        BigDecimal expectedDiscount = result.getPromotionValue().min(result.getSubtotal());
        assertThat(result.getDiscount()).isEqualByComparingTo(expectedDiscount);
    }

    /**
     * @brief Test hierarchical pricing - store-specific overrides global
     * London store should have different pricing than global
     */
    @Test
    public void testHierarchicalPricing_StoreOverridesGlobal() {
        // Get price for London store (store-specific rule)
        PriceCalculationRequest londonRequest = PriceCalculationRequest.builder()
                .itemId(10) // Item with both global and London-specific rules
                .storeId(1) // London Central
                .quantity(1)
                .build();

        ResponseEntity<PriceCalculationResponse> londonResponse = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(londonRequest, createHeaders()),
                PriceCalculationResponse.class
        );

        // Get price for different store (should use global rule)
        PriceCalculationRequest birminghamRequest = PriceCalculationRequest.builder()
                .itemId(10)
                .storeId(3) // Birmingham Store
                .quantity(1)
                .build();

        ResponseEntity<PriceCalculationResponse> birminghamResponse = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(birminghamRequest, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(londonResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(birminghamResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        PriceCalculationResponse londonResult = londonResponse.getBody();
        PriceCalculationResponse birminghamResult = birminghamResponse.getBody();

        assertThat(londonResult).isNotNull();
        assertThat(birminghamResult).isNotNull();

        // London should use store-specific rule, Birmingham should use global
        assertThat(londonResult.getRuleSource()).isEqualTo("STORE_SPECIFIC");
        assertThat(birminghamResult.getRuleSource()).isEqualTo("GLOBAL");

        // Prices should differ (store-specific overrides)
        assertThat(londonResult.getUnitPrice()).isNotEqualByComparingTo(birminghamResult.getUnitPrice());
    }

    /**
     * @brief Test invalid item ID returns 404
     */
    @Test
    public void testPriceCalculation_InvalidItemId() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(9999) // Non-existent item
                .storeId(1)
                .quantity(1)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * @brief Test invalid quantity (zero or negative) returns 400
     */
    @Test
    public void testPriceCalculation_InvalidQuantity() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(1)
                .storeId(1)
                .quantity(0) // Invalid quantity
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Test price calculation consistency - same input always gives same output
     */
    @Test
    public void testPriceCalculation_Consistency() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(1)
                .storeId(1)
                .quantity(3)
                .build();

        ResponseEntity<PriceCalculationResponse> response1 = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        ResponseEntity<PriceCalculationResponse> response2 = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        PriceCalculationResponse result1 = response1.getBody();
        PriceCalculationResponse result2 = response2.getBody();

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();

        // All fields should be identical
        assertThat(result1.getFinalPrice()).isEqualByComparingTo(result2.getFinalPrice());
        assertThat(result1.getDiscount()).isEqualByComparingTo(result2.getDiscount());
        assertThat(result1.getPromotionApplied()).isEqualTo(result2.getPromotionApplied());
    }

    /**
     * @brief Test large quantity calculation (performance and correctness)
     */
    @Test
    public void testPriceCalculation_LargeQuantity() {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .itemId(1)
                .storeId(1)
                .quantity(1000)
                .build();

        ResponseEntity<PriceCalculationResponse> response = restTemplate.postForEntity(
                "/api/pricing/calculate",
                new HttpEntity<>(request, createHeaders()),
                PriceCalculationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        PriceCalculationResponse result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(1000);
        
        // Total price should be unit price * 1000 (assuming no promotion for item 1)
        BigDecimal expectedPrice = result.getUnitPrice().multiply(new BigDecimal("1000"));
        assertThat(result.getFinalPrice()).isEqualByComparingTo(expectedPrice);
    }
}
