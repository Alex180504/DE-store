package com.destore.inventory.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Multi-datasource configuration for Inventory Service
 * 
 * Primary: Warehouse DB (MySQL - Read-Only)
 * Secondary: Auth DB (PostgreSQL - Read emails for network managers)
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.destore.inventory.repository.warehouse",
    entityManagerFactoryRef = "warehouseEntityManagerFactory",
    transactionManagerRef = "warehouseTransactionManager"
)
public class DataSourceConfig {

    /** 
     * @return DataSourceProperties
     */
    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties warehouseDataSourceProperties() {
        return new DataSourceProperties();
    }

    /** 
     * @return DataSource
     */
    @Primary
    @Bean
    public DataSource warehouseDataSource() {
        return warehouseDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    /** 
     * @param warehouseEntityManagerFactory(
     * @return LocalContainerEntityManagerFactoryBean
     */
    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean warehouseEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("warehouseDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        
        return builder
                .dataSource(dataSource)
                .packages("com.destore.inventory.model.warehouse")
                .persistenceUnit("warehouse")
                .properties(properties)
                .build();
    }

    /** 
     * @param warehouseTransactionManager(
     * @return PlatformTransactionManager
     */
    @Primary
    @Bean
    public PlatformTransactionManager warehouseTransactionManager(
            @Qualifier("warehouseEntityManagerFactory") LocalContainerEntityManagerFactoryBean warehouseEntityManagerFactory) {
        return new JpaTransactionManager(warehouseEntityManagerFactory.getObject());
    }
}
