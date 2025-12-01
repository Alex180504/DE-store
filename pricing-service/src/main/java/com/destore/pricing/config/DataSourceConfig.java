package com.destore.pricing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @file DataSourceConfig.java
 * @brief Configuration for multiple database connections
 * 
 * Configures two datasources:
 * 1. Primary: Pricing Database (PostgreSQL) - Read/Write
 * 2. Secondary: Warehouse Database (MySQL) - Read-Only
 * 
 * This allows the pricing service to validate items against the legacy
 * warehouse database while managing its own pricing rules.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Configuration
public class DataSourceConfig {

    /**
     * @brief Primary datasource for Pricing Database (PostgreSQL)
     * 
     * This is the main database for storing pricing rules, stores, and promotions.
     * JPA/Hibernate uses this datasource by default.
     * 
     * @return Configured PostgreSQL DataSource
     */
    @Primary
    @Bean(name = "pricingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource pricingDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * @brief Secondary datasource for Warehouse Database (MySQL - Read-Only)
     * 
     * This datasource connects to the legacy warehouse database for item validation.
     * Should only be used for SELECT queries to verify item_id exists.
     * 
     * @return Configured MySQL DataSource (Read-Only)
     */
    @Bean(name = "warehouseDataSource")
    @ConfigurationProperties(prefix = "pricing.warehouse.datasource")
    public DataSource warehouseDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * @brief JdbcTemplate for executing queries against Warehouse DB
     * 
     * Provides a simple interface for executing read-only SQL queries
     * against the MySQL warehouse database.
     * 
     * @param warehouseDataSource The warehouse database datasource
     * @return Configured JdbcTemplate instance
     */
    @Bean(name = "warehouseJdbcTemplate")
    public JdbcTemplate warehouseJdbcTemplate(DataSource warehouseDataSource) {
        return new JdbcTemplate(warehouseDataSource);
    }
}
