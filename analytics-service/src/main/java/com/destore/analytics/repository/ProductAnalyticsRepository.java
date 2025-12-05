package com.destore.analytics.repository;

import com.destore.analytics.dto.ProductPerformanceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for product performance analytics using accounting and warehouse databases.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductAnalyticsRepository {

    @Qualifier("accountingJdbcTemplate")
    private final JdbcTemplate accountingJdbcTemplate;

    @Qualifier("warehouseJdbcTemplate")
    private final JdbcTemplate warehouseJdbcTemplate;

    /**
     * Get top-selling products for a store within date range.
     * Joins accounting transaction_items with warehouse items for product details.
     */
    public List<ProductPerformanceDTO.TopProduct> getTopProducts(Long storeId, LocalDate startDate, LocalDate endDate, int limit) {
        // First, get top item IDs and their metrics from accounting DB
        String accountingSql = """
            SELECT 
                ti.item_id,
                SUM(ti.quantity) as total_quantity,
                SUM(ti.subtotal) as total_revenue,
                AVG(ti.unit_price) as avg_price,
                COUNT(DISTINCT t.id) as transaction_count
            FROM transaction_items ti
            JOIN transactions t ON ti.transaction_id = t.id
            WHERE t.store_id = ?
                AND t.transaction_date >= ?
                AND t.transaction_date <= ?
                AND t.status = 'COMPLETED'
            GROUP BY ti.item_id
            ORDER BY total_revenue DESC
            LIMIT ?
            """;

        List<TopProductData> topProductData = accountingJdbcTemplate.query(accountingSql,
            (rs, rowNum) -> {
                TopProductData data = new TopProductData();
                data.itemId = rs.getLong("item_id");
                data.totalQuantity = rs.getInt("total_quantity");
                data.totalRevenue = rs.getBigDecimal("total_revenue");
                data.avgPrice = rs.getBigDecimal("avg_price");
                data.transactionCount = rs.getLong("transaction_count");
                return data;
            },
            storeId, startDate, endDate, limit);

        // Enrich with product details from warehouse DB
        return topProductData.stream()
            .map(data -> {
                String itemName = "Unknown Product";
                String category = "Unknown";
                
                try {
                    String warehouseSql = "SELECT name, category FROM items WHERE item_id = ?";
                    var productInfo = warehouseJdbcTemplate.queryForMap(warehouseSql, data.itemId);
                    itemName = (String) productInfo.get("name");
                    category = (String) productInfo.get("category");
                } catch (Exception e) {
                    log.warn("Could not find product details for item_id: {}", data.itemId);
                }

                return ProductPerformanceDTO.TopProduct.builder()
                    .itemId(data.itemId)
                    .itemName(itemName)
                    .category(category)
                    .quantitySold(data.totalQuantity)
                    .totalRevenue(data.totalRevenue)
                    .averagePrice(data.avgPrice)
                    .transactionCount(data.transactionCount)
                    .build();
            })
            .toList();
    }

    /**
     * Get category performance aggregated across all products.
     */
    public List<ProductPerformanceDTO.CategoryPerformance> getCategoryPerformance(Long storeId, LocalDate startDate, LocalDate endDate) {
        // Get item IDs and metrics from accounting
        String accountingSql = """
            SELECT 
                ti.item_id,
                SUM(ti.quantity) as total_quantity,
                SUM(ti.subtotal) as total_revenue,
                AVG(ti.unit_price) as avg_price
            FROM transaction_items ti
            JOIN transactions t ON ti.transaction_id = t.id
            WHERE t.store_id = ?
                AND t.transaction_date >= ?
                AND t.transaction_date <= ?
                AND t.status = 'COMPLETED'
            GROUP BY ti.item_id
            """;

        List<ItemMetrics> itemMetrics = accountingJdbcTemplate.query(accountingSql,
            (rs, rowNum) -> {
                ItemMetrics metrics = new ItemMetrics();
                metrics.itemId = rs.getLong("item_id");
                metrics.totalQuantity = rs.getInt("total_quantity");
                metrics.totalRevenue = rs.getBigDecimal("total_revenue");
                metrics.avgPrice = rs.getBigDecimal("avg_price");
                return metrics;
            },
            storeId, startDate, endDate);

        // Get total revenue for percentage calculation
        BigDecimal totalRevenue = itemMetrics.stream()
            .map(m -> m.totalRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by category (fetching from warehouse)
        var categoryMap = new java.util.HashMap<String, CategoryData>();
        
        for (ItemMetrics metrics : itemMetrics) {
            String category = "Unknown";
            try {
                category = warehouseJdbcTemplate.queryForObject(
                    "SELECT category FROM items WHERE item_id = ?",
                    String.class,
                    metrics.itemId
                );
            } catch (Exception e) {
                log.debug("Category not found for item_id: {}", metrics.itemId);
            }

            categoryMap.computeIfAbsent(category, k -> new CategoryData())
                .add(metrics);
        }

        // Convert to DTOs
        return categoryMap.entrySet().stream()
            .map(entry -> {
                CategoryData data = entry.getValue();
                BigDecimal percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? data.totalRevenue.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

                return ProductPerformanceDTO.CategoryPerformance.builder()
                    .category(entry.getKey())
                    .totalQuantity(data.totalQuantity)
                    .totalRevenue(data.totalRevenue)
                    .uniqueProducts(data.uniqueProducts)
                    .averagePrice(data.getAveragePrice())
                    .percentageOfTotal(percentage)
                    .build();
            })
            .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
            .toList();
    }

    // Helper classes
    private static class TopProductData {
        Long itemId;
        Integer totalQuantity;
        BigDecimal totalRevenue;
        BigDecimal avgPrice;
        Long transactionCount;
    }

    private static class ItemMetrics {
        Long itemId;
        Integer totalQuantity;
        BigDecimal totalRevenue;
        BigDecimal avgPrice;
    }

    private static class CategoryData {
        Integer totalQuantity = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long uniqueProducts = 0L;
        BigDecimal totalAvgPrice = BigDecimal.ZERO;

        void add(ItemMetrics metrics) {
            this.totalQuantity += metrics.totalQuantity;
            this.totalRevenue = this.totalRevenue.add(metrics.totalRevenue);
            this.uniqueProducts++;
            this.totalAvgPrice = this.totalAvgPrice.add(metrics.avgPrice);
        }

        BigDecimal getAveragePrice() {
            return uniqueProducts > 0
                ? totalAvgPrice.divide(BigDecimal.valueOf(uniqueProducts), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        }
    }
}
