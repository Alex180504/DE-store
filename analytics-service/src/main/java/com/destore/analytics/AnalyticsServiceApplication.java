package com.destore.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Analytics Service - Performance reporting and analytics for DE-Store.
 * <p>
 * Provides read-only access to transaction data across accounting, store, and warehouse databases
 * to generate insights for network managers on revenue, transactions, products, and store performance.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@SpringBootApplication
public class AnalyticsServiceApplication {

    /** 
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
