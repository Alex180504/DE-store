package com.destore.inventory.controller;

import com.destore.inventory.model.dto.ThresholdUpdateRequest;
import com.destore.inventory.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Inventory Admin Controller
 * Tests JWT authentication, role-based authorization, and endpoint functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String networkManagerToken = "Bearer test-network-manager-token";
    private String storeManagerToken = "Bearer test-store-manager-token";

    @BeforeEach
    void setUp() {
        // Tokens are hardcoded for integration tests
        // In real tests, you would use @WithMockUser or custom security context
    }

    // ==================== Health Endpoint Tests ====================

    @Test
    @DisplayName("Health endpoint should be publicly accessible")
    void healthEndpoint_NoAuth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/inventory/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("inventory-service"));
    }

    // ==================== Authentication Tests ====================

    @Test
    @DisplayName("Admin endpoints should reject requests without JWT token")
    void adminEndpoints_NoAuth_Returns403() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/alerts"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/inventory/admin/trigger-check"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/inventory/admin/thresholds"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/inventory/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoints should reject requests with invalid JWT token")
    void adminEndpoints_InvalidToken_Returns403() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/alerts")
                        .header("Authorization", "Bearer invalid-token-here"))
                .andExpect(status().isForbidden());
    }

    // ==================== Authorization Tests ====================

    @Test
    @DisplayName("Admin endpoints should reject STORE_MANAGER role")
    void adminEndpoints_StoreManager_Returns403() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/alerts")
                        .header("Authorization", "Bearer " + storeManagerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/inventory/admin/trigger-check")
                        .header("Authorization", "Bearer " + storeManagerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/inventory/admin/stats")
                        .header("Authorization", "Bearer " + storeManagerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoints should accept NETWORK_MANAGER role")
    void adminEndpoints_NetworkManager_ReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/alerts")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/inventory/admin/thresholds")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStock").isNumber())
                .andExpect(jsonPath("$.criticalStock").isNumber());

        mockMvc.perform(get("/api/inventory/admin/stats")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlerts").isNumber())
                .andExpect(jsonPath("$.outOfStock").isNumber())
                .andExpect(jsonPath("$.critical").isNumber())
                .andExpect(jsonPath("$.low").isNumber());
    }

    // ==================== Alerts Endpoint Tests ====================

    @Test
    @DisplayName("GET /admin/alerts should return array of alerts")
    void getAlerts_NetworkManager_ReturnsAlertArray() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/alerts")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== Trigger Check Endpoint Tests ====================

    @Test
    @DisplayName("POST /admin/trigger-check should trigger manual stock check")
    void triggerCheck_NetworkManager_ReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/inventory/admin/trigger-check")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    // ==================== Thresholds Endpoint Tests ====================

    @Test
    @DisplayName("GET /admin/thresholds should return current configuration")
    void getThresholds_NetworkManager_ReturnsConfig() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/thresholds")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStock").isNumber())
                .andExpect(jsonPath("$.criticalStock").isNumber())
                .andExpect(jsonPath("$.lowStock").value(greaterThan(0)))
                .andExpect(jsonPath("$.criticalStock").value(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("PUT /admin/thresholds should update configuration with valid values")
    void updateThresholds_ValidValues_ReturnsSuccess() throws Exception {
        ThresholdUpdateRequest request = new ThresholdUpdateRequest();
        request.setLowStock(60);
        request.setCriticalStock(25);

        mockMvc.perform(put("/api/inventory/admin/thresholds")
                        .header("Authorization", "Bearer " + networkManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.thresholds.lowStock").value(60))
                .andExpect(jsonPath("$.thresholds.criticalStock").value(25));
    }

    @Test
    @DisplayName("PUT /admin/thresholds should reject when criticalStock >= lowStock")
    void updateThresholds_CriticalNotLessThanLow_ReturnsBadRequest() throws Exception {
        ThresholdUpdateRequest request = new ThresholdUpdateRequest();
        request.setLowStock(30);
        request.setCriticalStock(30); // Equal to lowStock

        mockMvc.perform(put("/api/inventory/admin/thresholds")
                        .header("Authorization", "Bearer " + networkManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(containsString("must be less than")));
    }

    @Test
    @DisplayName("PUT /admin/thresholds should reject when criticalStock > lowStock")
    void updateThresholds_CriticalGreaterThanLow_ReturnsBadRequest() throws Exception {
        ThresholdUpdateRequest request = new ThresholdUpdateRequest();
        request.setLowStock(30);
        request.setCriticalStock(40); // Greater than lowStock

        mockMvc.perform(put("/api/inventory/admin/thresholds")
                        .header("Authorization", "Bearer " + networkManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("PUT /admin/thresholds should reject negative values")
    void updateThresholds_NegativeValues_ReturnsBadRequest() throws Exception {
        ThresholdUpdateRequest request = new ThresholdUpdateRequest();
        request.setLowStock(-10);
        request.setCriticalStock(-5);

        mockMvc.perform(put("/api/inventory/admin/thresholds")
                        .header("Authorization", "Bearer " + networkManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/thresholds should reject missing fields")
    void updateThresholds_MissingFields_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"lowStock\": 50}"; // Missing criticalStock

        mockMvc.perform(put("/api/inventory/admin/thresholds")
                        .header("Authorization", "Bearer " + networkManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Statistics Endpoint Tests ====================

    @Test
    @DisplayName("GET /admin/stats should return complete statistics")
    void getStats_NetworkManager_ReturnsCompleteStats() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/stats")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlerts").isNumber())
                .andExpect(jsonPath("$.outOfStock").isNumber())
                .andExpect(jsonPath("$.critical").isNumber())
                .andExpect(jsonPath("$.low").isNumber())
                .andExpect(jsonPath("$.thresholds").exists())
                .andExpect(jsonPath("$.thresholds.lowStock").isNumber())
                .andExpect(jsonPath("$.thresholds.criticalStock").isNumber())
                .andExpect(jsonPath("$.totalAlerts").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.outOfStock").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.critical").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.low").value(greaterThanOrEqualTo(0)));
    }

    // ==================== Security - Insecure Endpoints Blocked ====================

    @Test
    @DisplayName("Old insecure /api/inventory/alerts endpoint should be blocked")
    void oldAlertsEndpoint_NoAuth_Returns403() throws Exception {
        mockMvc.perform(get("/api/inventory/alerts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Old insecure /api/inventory/check endpoint should be blocked")
    void oldCheckEndpoint_NoAuth_Returns403() throws Exception {
        mockMvc.perform(post("/api/inventory/check"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Old insecure endpoints should be blocked even with authentication")
    void oldEndpoints_WithAuth_Returns403() throws Exception {
        mockMvc.perform(get("/api/inventory/alerts")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/inventory/check")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isForbidden());
    }

    // ==================== CORS and Headers Tests ====================

    @Test
    @DisplayName("Admin endpoints should include security headers")
    void adminEndpoints_IncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/inventory/admin/stats")
                        .header("Authorization", "Bearer " + networkManagerToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"));
    }
}
