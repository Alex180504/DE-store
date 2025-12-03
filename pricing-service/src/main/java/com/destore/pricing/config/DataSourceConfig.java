package com.destore.pricing.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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
 * 1. Primary: Pricing Database (PostgreSQL) - Read/Write for JPA
 * 2. Secondary: Warehouse Database (MySQL) - Read-Only for item validation
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
     * @brief DataSource properties for primary database
     * 
     * @return DataSource properties from spring.datasource.* configuration
     */
    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * @brief Primary datasource for Pricing Database (PostgreSQL)
     * 
     * This is marked as @Primary to ensure JPA/Hibernate uses this datasource
     * and not the warehouse datasource.
     * 
     * @return Configured PostgreSQL DataSource
     */
    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
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
    public HikariDataSource warehouseDataSource(
            @Value("${pricing.warehouse.datasource.url}") String url,
            @Value("${pricing.warehouse.datasource.username}") String username,
            @Value("${pricing.warehouse.datasource.password}") String password,
            @Value("${pricing.warehouse.datasource.driver-class-name}") String driverClassName) {
        
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .type(HikariDataSource.class)
                .build();
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
    public JdbcTemplate warehouseJdbcTemplate(@Qualifier("warehouseDataSource") DataSource warehouseDataSource) {
        return new JdbcTemplate(warehouseDataSource);
    }
}
