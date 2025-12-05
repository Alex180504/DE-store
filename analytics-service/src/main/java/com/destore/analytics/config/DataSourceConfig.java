package com.destore.analytics.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Multi-datasource configuration for analytics service.
 * <p>
 * Configures read-only connections to:
 * - Accounting DB (primary data source for transactions)
 * - Store DB (store metadata)
 * - Warehouse DB (product metadata)
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Configuration
public class DataSourceConfig {

    /**
     * Accounting database datasource (primary).
     * Contains transaction and transaction_items data.
     */
    @Primary
    @Bean(name = "accountingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.accounting")
    public DataSource accountingDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Store database datasource.
     * Contains store metadata (names, locations, etc.).
     */
    @Bean(name = "storeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.store")
    public DataSource storeDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Warehouse database datasource.
     * Contains product/item metadata (names, categories, prices).
     */
    @Bean(name = "warehouseDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.warehouse")
    public DataSource warehouseDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * JdbcTemplate for accounting database queries.
     */
    @Primary
    @Bean(name = "accountingJdbcTemplate")
    public JdbcTemplate accountingJdbcTemplate(DataSource accountingDataSource) {
        return new JdbcTemplate(accountingDataSource);
    }

    /**
     * JdbcTemplate for store database queries.
     */
    @Bean(name = "storeJdbcTemplate")
    public JdbcTemplate storeJdbcTemplate(DataSource storeDataSource) {
        return new JdbcTemplate(storeDataSource);
    }

    /**
     * JdbcTemplate for warehouse database queries.
     */
    @Bean(name = "warehouseJdbcTemplate")
    public JdbcTemplate warehouseJdbcTemplate(DataSource warehouseDataSource) {
        return new JdbcTemplate(warehouseDataSource);
    }
}
