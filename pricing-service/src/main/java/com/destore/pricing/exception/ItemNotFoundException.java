package com.destore.pricing.exception;

/**
 * @file ItemNotFoundException.java
 * @brief Exception thrown when an item is not found in the warehouse database
 * 
 * This exception is raised when attempting to create a pricing rule for
 * a non-existent item, or when calculating prices for invalid item IDs.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
public class ItemNotFoundException extends RuntimeException {

    /**
     * @brief Construct exception with item ID
     * @param itemId The item ID that was not found
     */
    public ItemNotFoundException(Integer itemId) {
        super("Item not found in warehouse database: itemId=" + itemId);
    }

    /**
     * @brief Construct exception with custom message
     * @param message The error message
     */
    public ItemNotFoundException(String message) {
        super(message);
    }
}
