package com.destore.analytics.repository;

import com.destore.analytics.dto.StoreComparisonDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository for store comparison and network-wide analytics.
 */
@Repository
@Slf4j
public class StoreAnalyticsRepository {

    private final JdbcTemplate accountingJdbcTemplate;
    private final JdbcTemplate storeJdbcTemplate;

    public StoreAnalyticsRepository(
            @Qualifier("accountingJdbcTemplate") JdbcTemplate accountingJdbcTemplate,
            @Qualifier("storeJdbcTemplate") JdbcTemplate storeJdbcTemplate) {
        this.accountingJdbcTemplate = accountingJdbcTemplate;
        this.storeJdbcTemplate = storeJdbcTemplate;
    }

    /**
     * Get performance metrics for all stores in the network.
     */
    public List<StoreMetricsData> getAllStoreMetrics(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                store_id,
                COALESCE(SUM(total_amount), 0) as revenue,
                COUNT(*) as transaction_count,
                COALESCE(AVG(total_amount), 0) as avg_transaction_value
            FROM transactions
            WHERE transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            GROUP BY store_id
            ORDER BY revenue DESC
            """;

        List<StoreMetricsData> metrics = accountingJdbcTemplate.query(sql,
            (rs, rowNum) -> {
                StoreMetricsData data = new StoreMetricsData();
                data.storeId = rs.getLong("store_id");
                data.revenue = rs.getBigDecimal("revenue");
                data.transactionCount = rs.getLong("transaction_count");
                data.avgTransactionValue = rs.getBigDecimal("avg_transaction_value");
                return data;
            },
            startDate, endDate);

        // Enrich with store details
        for (StoreMetricsData data : metrics) {
            enrichStoreDetails(data);
        }

        // Calculate rankings and percentages
        BigDecimal totalRevenue = metrics.stream()
            .map(m -> m.revenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        AtomicInteger rank = new AtomicInteger(1);
        metrics.forEach(data -> {
            data.rank = rank.getAndIncrement();
            data.percentageOfNetwork = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? data.revenue.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        });

        return metrics;
    }

    /**
     * Get performance metrics for a specific store.
     */
    public StoreMetricsData getStoreMetrics(Long storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                store_id,
                COALESCE(SUM(total_amount), 0) as revenue,
                COUNT(*) as transaction_count,
                COALESCE(AVG(total_amount), 0) as avg_transaction_value
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            GROUP BY store_id
            """;

        StoreMetricsData data = accountingJdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> {
                StoreMetricsData d = new StoreMetricsData();
                d.storeId = rs.getLong("store_id");
                d.revenue = rs.getBigDecimal("revenue");
                d.transactionCount = rs.getLong("transaction_count");
                d.avgTransactionValue = rs.getBigDecimal("avg_transaction_value");
                return d;
            },
            storeId, startDate, endDate);

        enrichStoreDetails(data);
        return data;
    }

    /**
     * Calculate growth rate for a store compared to previous period.
     */
    public BigDecimal calculateGrowthRate(Long storeId, LocalDate startDate, LocalDate endDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevStartDate = startDate.minusDays(daysBetween);
        LocalDate prevEndDate = endDate.minusDays(daysBetween);

        String sql = """
            SELECT COALESCE(SUM(total_amount), 0) as revenue
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            """;

        BigDecimal currentRevenue = accountingJdbcTemplate.queryForObject(sql, BigDecimal.class, 
            storeId, startDate, endDate);
        BigDecimal previousRevenue = accountingJdbcTemplate.queryForObject(sql, BigDecimal.class,
            storeId, prevStartDate, prevEndDate);

        if (previousRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return currentRevenue.subtract(previousRevenue)
            .divide(previousRevenue, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Get network-wide totals.
     */
    public NetworkTotals getNetworkTotals(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(SUM(total_amount), 0) as total_revenue,
                COUNT(*) as total_transactions,
                COUNT(DISTINCT store_id) as store_count
            FROM transactions
            WHERE transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            """;

        return accountingJdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> {
                NetworkTotals totals = new NetworkTotals();
                totals.totalRevenue = rs.getBigDecimal("total_revenue");
                totals.totalTransactions = rs.getLong("total_transactions");
                totals.storeCount = rs.getInt("store_count");
                return totals;
            },
            startDate, endDate);
    }

    /** 
     * @param data
     */
    private void enrichStoreDetails(StoreMetricsData data) {
        try {
            String sql = "SELECT store_name, address, postcode FROM stores WHERE store_id = ?";
            var storeInfo = storeJdbcTemplate.queryForMap(sql, data.storeId);
            data.storeName = (String) storeInfo.get("store_name");
            data.location = (String) storeInfo.get("address");
            data.region = (String) storeInfo.get("postcode");
        } catch (Exception e) {
            log.warn("Could not find store details for ID: {}", data.storeId);
            data.storeName = "Unknown Store";
            data.location = "Unknown";
            data.region = "Unknown";
        }
    }

    // Helper classes
    public static class StoreMetricsData {
        public Long storeId;
        public String storeName;
        public String location;
        public String region;
        public BigDecimal revenue;
        public Long transactionCount;
        public BigDecimal avgTransactionValue;
        public Integer rank;
        public BigDecimal percentageOfNetwork;
        public BigDecimal growthRate;
    }

    public static class NetworkTotals {
        public BigDecimal totalRevenue;
        public Long totalTransactions;
        public Integer storeCount;
    }

    /**
     * Simple store information for dropdown lists.
     */
    public static class StoreInfo {
        public Long id;
        public String name;
        public String location;
    }

    /**
     * Get all stores for dropdown selection.
     */
    public List<StoreInfo> getAllStores() {
        String sql = """
            SELECT store_id, store_name, address
            FROM stores
            WHERE is_active = true
            ORDER BY store_name
            """;

        return storeJdbcTemplate.query(sql, (rs, rowNum) -> {
            StoreInfo store = new StoreInfo();
            store.id = rs.getLong("store_id");
            store.name = rs.getString("store_name");
            store.location = rs.getString("address");
            return store;
        });
    }
}
