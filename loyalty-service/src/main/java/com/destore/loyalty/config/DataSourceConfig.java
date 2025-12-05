package com.destore.loyalty.config;

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
 * Configuration for dual datasources (loyalty DB and accounting DB).
 * <p>
 * Primary datasource: loyalty-db (read/write) - for loyalty data
 * Secondary datasource: accounting-db (read-only) - for transaction queries
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.destore.loyalty.repository",
    entityManagerFactoryRef = "loyaltyEntityManagerFactory",
    transactionManagerRef = "loyaltyTransactionManager"
)
public class DataSourceConfig {

    /**
     * Primary datasource properties for loyalty database.
     *
     * @return DataSource properties from application.properties
     */
    @Primary
    @Bean(name = "loyaltyDataSourceProperties")
    @ConfigurationProperties("spring.datasource.loyalty")
    public DataSourceProperties loyaltyDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Primary datasource bean for loyalty database.
     *
     * @param properties Loyalty datasource properties
     * @return Configured DataSource
     */
    @Primary
    @Bean(name = "loyaltyDataSource")
    public DataSource loyaltyDataSource(
            @Qualifier("loyaltyDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * Primary entity manager factory for loyalty entities.
     *
     * @param builder EntityManagerFactoryBuilder
     * @param dataSource Loyalty datasource
     * @return LocalContainerEntityManagerFactoryBean
     */
    @Primary
    @Bean(name = "loyaltyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean loyaltyEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("loyaltyDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        
        return builder
                .dataSource(dataSource)
                .packages("com.destore.loyalty.entity")
                .persistenceUnit("loyalty")
                .properties(properties)
                .build();
    }

    /**
     * Primary transaction manager for loyalty database.
     *
     * @param entityManagerFactory Loyalty entity manager factory
     * @return PlatformTransactionManager
     */
    @Primary
    @Bean(name = "loyaltyTransactionManager")
    public PlatformTransactionManager loyaltyTransactionManager(
            @Qualifier("loyaltyEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    /**
     * Secondary datasource properties for accounting database.
     *
     * @return DataSource properties from application.properties
     */
    @Bean(name = "accountingDataSourceProperties")
    @ConfigurationProperties("spring.datasource.accounting")
    public DataSourceProperties accountingDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Secondary datasource bean for accounting database (read-only).
     *
     * @param properties Accounting datasource properties
     * @return Configured DataSource
     */
    @Bean(name = "accountingDataSource")
    public DataSource accountingDataSource(
            @Qualifier("accountingDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
