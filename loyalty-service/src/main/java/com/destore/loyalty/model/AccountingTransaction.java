package com.destore.loyalty.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model representing a transaction from the accounting database.
 * <p>
 * Used for read-only queries from the accounting datasource.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@Getter
@Setter
public class AccountingTransaction {

    /**
     * Transaction ID.
     */
    private Long transactionId;

    /**
     * Customer ID.
     */
    private Integer customerId;

    /**
     * Store ID.
     */
    private Integer storeId;

    /**
     * Transaction timestamp.
     */
    private LocalDateTime transactionDate;

    /**
     * Total transaction amount.
     */
    private BigDecimal totalAmount;

    /**
     * Transaction status.
     */
    private String status;
}
