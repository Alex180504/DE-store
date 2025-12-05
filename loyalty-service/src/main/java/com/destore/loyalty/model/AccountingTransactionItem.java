package com.destore.loyalty.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Model representing a transaction item from the accounting database.
 * <p>
 * Used for read-only queries from the accounting datasource.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
public class AccountingTransactionItem {

    /**
     * Item ID from transaction_items table.
     */
    private Integer itemId;

    /**
     * Transaction ID (foreign key).
     */
    private Long transactionId;

    /**
     * Product ID from warehouse.
     */
    private Integer productId;

    /**
     * Quantity purchased.
     */
    private Integer quantity;

    /**
     * Unit price at time of purchase.
     */
    private BigDecimal unitPrice;

    /**
     * Total line price.
     */
    private BigDecimal totalPrice;
}
