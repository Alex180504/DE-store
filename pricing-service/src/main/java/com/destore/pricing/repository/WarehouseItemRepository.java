package com.destore.pricing.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @file WarehouseItemRepository.java
 * @brief Repository for accessing warehouse database (MySQL - Read-Only)
 * 
 * Provides read-only access to the legacy warehouse database for item validation.
 * Uses JDBC template to execute simple SELECT queries against MySQL.
 * 
 * This is an anti-corruption layer pattern to isolate the pricing service
 * from the legacy warehouse database schema.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class WarehouseItemRepository {

    @Qualifier("warehouseJdbcTemplate")
    private final JdbcTemplate warehouseJdbcTemplate;

    /**
     * @brief Check if an item exists in the warehouse database
     * 
     * Validates that an item_id exists before allowing pricing rules to be created.
     * This prevents orphaned pricing rules for non-existent items.
     * 
     * @param itemId The warehouse item ID to check
     * @return true if item exists in warehouse database
     */
    public boolean existsById(Integer itemId) {
        String sql = "SELECT COUNT(*) FROM items WHERE item_id = ?";
        try {
            Integer count = warehouseJdbcTemplate.queryForObject(sql, Integer.class, itemId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking if item exists: itemId={}", itemId, e);
            return false;
        }
    }

    /**
     * @brief Get item name from warehouse database
     * 
     * Retrieves the item name for display purposes.
     * This is optional and used for enriching API responses.
     * 
     * @param itemId The warehouse item ID
     * @return Optional containing item name if found
     */
    public Optional<String> getItemName(Integer itemId) {
        String sql = "SELECT name FROM items WHERE item_id = ?";
        try {
            String name = warehouseJdbcTemplate.queryForObject(sql, String.class, itemId);
            return Optional.ofNullable(name);
        } catch (Exception e) {
            log.warn("Could not retrieve item name: itemId={}", itemId);
            return Optional.empty();
        }
    }

    /**
     * @brief Get item base price from warehouse database
     * 
     * Retrieves the warehouse base price for an item.
     * This can be used as a fallback when no pricing rule exists.
     * 
     * @param itemId The warehouse item ID
     * @return Optional containing base price if found
     */
    public Optional<java.math.BigDecimal> getItemBasePrice(Integer itemId) {
        String sql = "SELECT base_price FROM items WHERE item_id = ?";
        try {
            java.math.BigDecimal price = warehouseJdbcTemplate.queryForObject(
                sql, java.math.BigDecimal.class, itemId
            );
            return Optional.ofNullable(price);
        } catch (Exception e) {
            log.warn("Could not retrieve item base price: itemId={}", itemId);
            return Optional.empty();
        }
    }
}
