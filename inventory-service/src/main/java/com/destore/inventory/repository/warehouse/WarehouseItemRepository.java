package com.destore.inventory.repository.warehouse;

import com.destore.inventory.model.warehouse.WarehouseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for warehouse items - READ ONLY
 */
@Repository
public interface WarehouseItemRepository extends JpaRepository<WarehouseItem, Integer> {

    /**
     * Find all items with stock below threshold
     */
    @Query("SELECT w FROM WarehouseItem w WHERE w.stockQuantity <= :threshold AND w.stockQuantity > 0")
    List<WarehouseItem> findLowStockItems(@Param("threshold") int threshold);

    /**
     * Find all out-of-stock items
     */
    @Query("SELECT w FROM WarehouseItem w WHERE w.stockQuantity = 0")
    List<WarehouseItem> findOutOfStockItems();

    /**
     * Find items by category with low stock
     */
    @Query("SELECT w FROM WarehouseItem w WHERE w.category = :category AND w.stockQuantity <= :threshold")
    List<WarehouseItem> findLowStockByCategory(@Param("category") String category, @Param("threshold") int threshold);

    /**
     * Get all categories
     */
    @Query("SELECT DISTINCT w.category FROM WarehouseItem w")
    List<String> findAllCategories();
}
