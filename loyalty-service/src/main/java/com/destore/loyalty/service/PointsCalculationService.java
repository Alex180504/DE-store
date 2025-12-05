package com.destore.loyalty.service;

import com.destore.loyalty.entity.*;
import com.destore.loyalty.model.AccountingTransaction;
import com.destore.loyalty.model.AccountingTransactionItem;
import com.destore.loyalty.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating and updating customer loyalty points.
 * <p>
 * Handles scheduled batch updates and on-demand calculations by analyzing
 * customer purchases from the accounting database and applying active
 * loyalty rules at the time of each transaction.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsCalculationService {

    private final CustomerPointsBalanceRepository balanceRepository;
    private final ProductPointsRuleRepository ruleRepository;
    private final BonusOfferRepository bonusRepository;
    private final PointsTransactionRepository transactionRepository;

    @Qualifier("accountingDataSource")
    private final DataSource accountingDataSource;

    /**
     * Scheduled task to calculate points for all customers.
     * <p>
     * Runs every 6 hours as configured in application.properties.
     * Processes transactions since last calculation timestamp.
     * </p>
     */
    @Scheduled(cron = "${loyalty.scheduler.points-calculation-cron}")
    @Transactional
    public void scheduledPointsCalculation() {
        log.info("Starting scheduled points calculation");
        
        List<CustomerPointsBalance> allBalances = balanceRepository.findAll();
        
        for (CustomerPointsBalance balance : allBalances) {
            try {
                calculatePointsForCustomer(balance.getCustomerId());
            } catch (Exception e) {
                log.error("Error calculating points for customer {}: {}", 
                         balance.getCustomerId(), e.getMessage(), e);
            }
        }
        
        log.info("Completed scheduled points calculation for {} customers", allBalances.size());
    }

    /**
     * Calculates points for a specific customer.
     * <p>
     * Queries accounting database for transactions since last calculation,
     * applies product points rules and bonus offers valid at transaction time,
     * and updates customer balance.
     * </p>
     *
     * @param customerId Customer ID to calculate points for
     * @return Updated customer points balance
     */
    @Transactional
    public CustomerPointsBalance calculatePointsForCustomer(Long customerId) {
        log.info("Calculating points for customer {}", customerId);
        
        // Get or create customer balance
        CustomerPointsBalance balance = balanceRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    CustomerPointsBalance newBalance = CustomerPointsBalance.builder()
                            .customerId(customerId)
                            .currentBalance(BigDecimal.ZERO)
                            .lifetimeEarned(BigDecimal.ZERO)
                            .lifetimeRedeemed(BigDecimal.ZERO)
                            .build();
                    return balanceRepository.save(newBalance);
                });
        
        // Determine start date for incremental calculation
        LocalDateTime startDate = balance.getLastCalculatedAt() != null 
                ? balance.getLastCalculatedAt() 
                : LocalDateTime.of(2020, 1, 1, 0, 0); // Default to beginning of time
        
        LocalDateTime now = LocalDateTime.now();
        
        // Query accounting database for transactions
        List<AccountingTransaction> transactions = getAccountingTransactions(customerId, startDate, now);
        
        if (transactions.isEmpty()) {
            log.info("No new transactions for customer {}", customerId);
            balance.setLastCalculatedAt(now);
            return balanceRepository.save(balance);
        }
        
        log.info("Processing {} transactions for customer {}", transactions.size(), customerId);
        
        BigDecimal totalPointsEarned = BigDecimal.ZERO;
        
        // Process each transaction
        for (AccountingTransaction transaction : transactions) {
            BigDecimal transactionPoints = calculateTransactionPoints(transaction);
            totalPointsEarned = totalPointsEarned.add(transactionPoints);
            
            // Record points transaction
            PointsTransaction pointsTx = PointsTransaction.builder()
                    .customerId(customerId)
                    .transactionType(PointsTransaction.TransactionType.EARNED)
                    .pointsAmount(transactionPoints)
                    .sourceTransactionId(transaction.getTransactionId())
                    .storeId(transaction.getStoreId())
                    .description("Points earned from purchase")
                    .build();
            transactionRepository.save(pointsTx);
        }
        
        // Update balance
        balance.creditPoints(totalPointsEarned);
        balance.setLastCalculatedAt(now);
        
        log.info("Awarded {} points to customer {}", totalPointsEarned, customerId);
        
        return balanceRepository.save(balance);
    }

    /**
     * Calculates points for a single transaction.
     * <p>
     * Applies product points rules to each line item and bonus offers
     * to the transaction total, using rules valid at the transaction timestamp.
     * </p>
     *
     * @param transaction Accounting transaction
     * @return Total points earned from transaction
     */
    private BigDecimal calculateTransactionPoints(AccountingTransaction transaction) {
        BigDecimal totalPoints = BigDecimal.ZERO;
        
        // Get transaction items
        List<AccountingTransactionItem> items = getTransactionItems(transaction.getTransactionId());
        
        // Calculate product points for each item
        for (AccountingTransactionItem item : items) {
            List<ProductPointsRule> applicableRules = ruleRepository.findApplicableRules(
                    item.getProductId(),
                    transaction.getStoreId(),
                    transaction.getTransactionDate()
            );
            
            if (!applicableRules.isEmpty()) {
                // Use first matching rule (most specific)
                ProductPointsRule rule = applicableRules.get(0);
                BigDecimal itemPoints = rule.calculatePoints(
                        BigDecimal.valueOf(item.getQuantity()),
                        item.getTotalPrice()
                );
                totalPoints = totalPoints.add(itemPoints);
                
                log.debug("Item {} earned {} points using rule {}", 
                         item.getProductId(), itemPoints, rule.getRuleId());
            }
        }
        
        // Check for bonus offers
        List<BonusOffer> applicableBonuses = bonusRepository.findApplicableBonuses(
                transaction.getStoreId(),
                transaction.getTransactionDate()
        );
        
        for (BonusOffer bonus : applicableBonuses) {
            if (bonus.qualifiesForBonus(transaction.getTotalAmount())) {
                totalPoints = totalPoints.add(bonus.getBonusPoints());
                
                // Record bonus transaction
                PointsTransaction bonusTx = PointsTransaction.builder()
                        .customerId(transaction.getCustomerId())
                        .transactionType(PointsTransaction.TransactionType.BONUS)
                        .pointsAmount(bonus.getBonusPoints())
                        .sourceTransactionId(transaction.getTransactionId())
                        .bonusId(bonus.getBonusId())
                        .storeId(transaction.getStoreId())
                        .description("Bonus: " + bonus.getOfferName())
                        .build();
                transactionRepository.save(bonusTx);
                
                log.debug("Transaction {} earned {} bonus points from {}", 
                         transaction.getTransactionId(), bonus.getBonusPoints(), bonus.getOfferName());
                
                break; // Only apply highest qualifying bonus
            }
        }
        
        return totalPoints;
    }

    /**
     * Queries accounting database for customer transactions in date range.
     *
     * @param customerId Customer ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @return List of transactions
     */
    private List<AccountingTransaction> getAccountingTransactions(
            Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(accountingDataSource);
        
        String sql = "SELECT transaction_id, customer_id, store_id, transaction_date, total_amount, status " +
                     "FROM transactions " +
                     "WHERE customer_id = ? " +
                     "AND transaction_date >= ? " +
                     "AND transaction_date < ? " +
                     "AND status = 'COMPLETED' " +
                     "ORDER BY transaction_date ASC";
        
        return jdbcTemplate.query(sql, 
            (rs, rowNum) -> {
                AccountingTransaction tx = new AccountingTransaction();
                tx.setTransactionId(rs.getLong("transaction_id"));
                tx.setCustomerId(rs.getLong("customer_id"));
                tx.setStoreId(rs.getLong("store_id"));
                tx.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                tx.setTotalAmount(rs.getBigDecimal("total_amount"));
                tx.setStatus(rs.getString("status"));
                return tx;
            },
            customerId, startDate, endDate
        );
    }

    /**
     * Queries accounting database for transaction items.
     *
     * @param transactionId Transaction ID
     * @return List of transaction items
     */
    private List<AccountingTransactionItem> getTransactionItems(Long transactionId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(accountingDataSource);
        
        String sql = "SELECT item_id, transaction_id, product_id, quantity, unit_price, total_price " +
                     "FROM transaction_items " +
                     "WHERE transaction_id = ?";
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> {
                AccountingTransactionItem item = new AccountingTransactionItem();
                item.setItemId(rs.getLong("item_id"));
                item.setTransactionId(rs.getLong("transaction_id"));
                item.setProductId(rs.getLong("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setTotalPrice(rs.getBigDecimal("total_price"));
                return item;
            },
            transactionId
        );
    }

    /**
     * Manually triggers points calculation for a customer.
     * <p>
     * Useful for testing or immediate recalculation after rule changes.
     * </p>
     *
     * @param customerId Customer ID
     * @return Updated balance
     */
    public CustomerPointsBalance recalculateCustomerPoints(Long customerId) {
        log.info("Manual recalculation triggered for customer {}", customerId);
        return calculatePointsForCustomer(customerId);
    }
}
