package com.destore.analytics.repository;

import com.destore.analytics.dto.TransactionSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for transaction analytics queries.
 */
@Repository
@Slf4j
public class TransactionAnalyticsRepository {

    private final JdbcTemplate accountingJdbcTemplate;

    public TransactionAnalyticsRepository(
            @Qualifier("accountingJdbcTemplate") JdbcTemplate accountingJdbcTemplate) {
        this.accountingJdbcTemplate = accountingJdbcTemplate;
    }

    /**
     * Get transaction status breakdown with counts and volumes.
     */
    public TransactionStatusBreakdown getStatusBreakdown(Long storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                COUNT(*) as total_transactions,
                COALESCE(SUM(total_amount), 0) as total_volume,
                COALESCE(AVG(total_amount), 0) as average_value,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count,
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count,
                COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_count,
                COUNT(CASE WHEN status = 'REFUNDED' THEN 1 END) as refunded_count,
                COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN total_amount ELSE 0 END), 0) as completed_volume,
                COALESCE(SUM(CASE WHEN status = 'PENDING' THEN total_amount ELSE 0 END), 0) as pending_volume,
                COALESCE(SUM(CASE WHEN status = 'CANCELLED' THEN total_amount ELSE 0 END), 0) as cancelled_volume,
                COALESCE(SUM(CASE WHEN status = 'REFUNDED' THEN total_amount ELSE 0 END), 0) as refunded_volume
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
            """;

        return accountingJdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> {
                TransactionStatusBreakdown breakdown = new TransactionStatusBreakdown();
                breakdown.totalTransactions = rs.getLong("total_transactions");
                breakdown.totalVolume = rs.getBigDecimal("total_volume");
                breakdown.averageValue = rs.getBigDecimal("average_value");
                breakdown.completedCount = rs.getLong("completed_count");
                breakdown.pendingCount = rs.getLong("pending_count");
                breakdown.cancelledCount = rs.getLong("cancelled_count");
                breakdown.refundedCount = rs.getLong("refunded_count");
                breakdown.completedVolume = rs.getBigDecimal("completed_volume");
                breakdown.pendingVolume = rs.getBigDecimal("pending_volume");
                breakdown.cancelledVolume = rs.getBigDecimal("cancelled_volume");
                breakdown.refundedVolume = rs.getBigDecimal("refunded_volume");
                return breakdown;
            },
            storeId, startDate, endDate);
    }

    /**
     * Get hourly transaction distribution for the date range.
     */
    public List<TransactionSummaryDTO.HourlyDistribution> getHourlyDistribution(Long storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                EXTRACT(HOUR FROM created_at) as hour,
                COUNT(*) as transaction_count,
                COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN total_amount ELSE 0 END), 0) as revenue
            FROM transactions
            WHERE store_id = ?
                AND transaction_date >= ?
                AND transaction_date <= ?
            GROUP BY EXTRACT(HOUR FROM created_at)
            ORDER BY hour
            """;

        return accountingJdbcTemplate.query(sql,
            (rs, rowNum) -> TransactionSummaryDTO.HourlyDistribution.builder()
                .hour(rs.getInt("hour"))
                .transactionCount(rs.getLong("transaction_count"))
                .revenue(rs.getBigDecimal("revenue"))
                .build(),
            storeId, startDate, endDate);
    }

    // Helper class for status breakdown
    public static class TransactionStatusBreakdown {
        public Long totalTransactions;
        public BigDecimal totalVolume;
        public BigDecimal averageValue;
        public Long completedCount;
        public Long pendingCount;
        public Long cancelledCount;
        public Long refundedCount;
        public BigDecimal completedVolume;
        public BigDecimal pendingVolume;
        public BigDecimal cancelledVolume;
        public BigDecimal refundedVolume;
    }
}
