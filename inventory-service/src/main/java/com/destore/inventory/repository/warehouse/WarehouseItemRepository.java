package com.destore.inventory.repository.warehouse;

import com.destore.inventory.model.warehouse.WarehouseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for warehouse items - READ ONLY.
 * <p>
 * Provides access to WarehouseItem entities in the warehouse database.
 * </p>
 */
@Repository
public interface WarehouseItemRepository extends JpaRepository<WarehouseItem, Integer> {

    /**
     * Finds all items with stock quantity less than or equal to the specified threshold, but greater than 0.
     *
     * @param threshold the stock quantity threshold
     * @return a list of items with low stock
     */
    @Query("SELECT w FROM WarehouseItem w WHERE w.stockQuantity <= :threshold AND w.stockQuantity > 0")
    List<WarehouseItem> findLowStockItems(@Param("threshold") int threshold);

    /**
     * Finds all items that are out of stock (quantity is 0).
     *
     * @return a list of out-of-stock items
     */
    @Query("SELECT w FROM WarehouseItem w WHERE w.stockQuantity = 0")
    List<WarehouseItem> findOutOfStockItems();

    /**
     * Finds items in a specific category with stock quantity less than or equal to the threshold.
     *
     * @param category  the item category
     * @param threshold the stock quantity threshold
     * @return a list of matching items
     */
    @Query("SELECT w FROM WarehouseItem w WHERE w.category = :category AND w.stockQuantity <= :threshold")
    List<WarehouseItem> findLowStockByCategory(@Param("category") String category, @Param("threshold") int threshold);

    /**
     * Retrieves all distinct item categories.
     *
     * @return a list of unique category names
     */
    @Query("SELECT DISTINCT w.category FROM WarehouseItem w")
    List<String> findAllCategories();
}
