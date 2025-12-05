package com.destore.analytics.repository;

import com.destore.analytics.dto.RevenueReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for revenue analytics queries using read-only accounting database.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RevenueAnalyticsRepository {

    @Qualifier("accountingJdbcTemplate")
    private final JdbcTemplate accountingJdbcTemplate;

    @Qualifier("storeJdbcTemplate")
    private final JdbcTemplate storeJdbcTemplate;

    /**
     * Get revenue summary for a specific store and date range.
     */
    public RevenueReportDTO.DailyRevenue getRevenueSummary(Long storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(SUM(total_amount), 0) as total_revenue,
                COUNT(*) as transaction_count
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            """;

        return accountingJdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> RevenueReportDTO.DailyRevenue.builder()
                .revenue(rs.getBigDecimal("total_revenue"))
                .transactionCount(rs.getLong("transaction_count"))
                .build(),
            storeId, startDate, endDate);
    }

    /**
     * Get daily revenue breakdown for trend analysis.
     */
    public List<RevenueReportDTO.DailyRevenue> getDailyRevenue(Long storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                transaction_date as date,
                COALESCE(SUM(total_amount), 0) as revenue,
                COUNT(*) as transaction_count
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            GROUP BY transaction_date
            ORDER BY transaction_date
            """;

        return accountingJdbcTemplate.query(sql,
            (rs, rowNum) -> RevenueReportDTO.DailyRevenue.builder()
                .date(rs.getDate("date").toLocalDate())
                .revenue(rs.getBigDecimal("revenue"))
                .transactionCount(rs.getLong("transaction_count"))
                .build(),
            storeId, startDate, endDate);
    }

    /**
     * Get payment method breakdown with revenue and transaction counts.
     */
    public List<RevenueReportDTO.PaymentMethodRevenue> getPaymentMethodBreakdown(Long storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                payment_method,
                COALESCE(SUM(total_amount), 0) as revenue,
                COUNT(*) as transaction_count,
                ROUND(
                    (COUNT(*) * 100.0 / NULLIF((SELECT COUNT(*) FROM transactions 
                        WHERE store_id = ? AND transaction_date >= ? AND transaction_date <= ? AND status = 'COMPLETED'), 0)), 
                    2
                ) as percentage
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            GROUP BY payment_method
            ORDER BY revenue DESC
            """;

        return accountingJdbcTemplate.query(sql,
            (rs, rowNum) -> RevenueReportDTO.PaymentMethodRevenue.builder()
                .paymentMethod(rs.getString("payment_method"))
                .revenue(rs.getBigDecimal("revenue"))
                .transactionCount(rs.getLong("transaction_count"))
                .percentage(rs.getBigDecimal("percentage"))
                .build(),
            storeId, startDate, endDate, storeId, startDate, endDate);
    }

    /**
     * Get revenue statistics including min, max, and average transaction values.
     */
    public RevenueStats getRevenueStats(Long storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COALESCE(SUM(total_amount), 0) as total_revenue,
                COUNT(*) as transaction_count,
                COALESCE(AVG(total_amount), 0) as avg_value,
                COALESCE(MIN(total_amount), 0) as min_value,
                COALESCE(MAX(total_amount), 0) as max_value
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
                AND status = 'COMPLETED'
            """;

        return accountingJdbcTemplate.queryForObject(sql, new RevenueStatsRowMapper(), storeId, startDate, endDate);
    }

    /**
     * Get store name from store database.
     */
    public String getStoreName(Long storeId) {
        String sql = "SELECT name FROM stores WHERE id = ?";
        try {
            return storeJdbcTemplate.queryForObject(sql, String.class, storeId);
        } catch (Exception e) {
            log.warn("Could not find store name for ID: {}", storeId);
            return "Unknown Store";
        }
    }

    // Helper class for revenue statistics
    public static class RevenueStats {
        public BigDecimal totalRevenue;
        public Long transactionCount;
        public BigDecimal averageValue;
        public BigDecimal minValue;
        public BigDecimal maxValue;
    }

    private static class RevenueStatsRowMapper implements RowMapper<RevenueStats> {
        @Override
        public RevenueStats mapRow(ResultSet rs, int rowNum) throws SQLException {
            RevenueStats stats = new RevenueStats();
            stats.totalRevenue = rs.getBigDecimal("total_revenue");
            stats.transactionCount = rs.getLong("transaction_count");
            stats.averageValue = rs.getBigDecimal("avg_value");
            stats.minValue = rs.getBigDecimal("min_value");
            stats.maxValue = rs.getBigDecimal("max_value");
            return stats;
        }
    }
}
