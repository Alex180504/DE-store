package com.destore.pricing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @file PricingServiceApplicationTests.java
 * @brief Basic smoke test for Spring Boot application context
 * 
 * Verifies that the application context loads successfully with all
 * required beans and configurations.
 */
@SpringBootTest
@ActiveProfiles("test")
class PricingServiceApplicationTests {

    /**
     * @brief Test that the application context loads without errors
     */
    @Test
    void contextLoads() {
        // Spring Boot will fail this test if context cannot load
    }
}
