package com.destore.store.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for store responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    /**
     * Store ID.
     */
    private Integer storeId;

    /**
     * Store code.
     */
    private String storeCode;

    /**
     * Store name.
     */
    private String storeName;

    /**
     * Store address.
     */
    private String address;

    /**
     * Store postcode.
     */
    private String postcode;

    /**
     * Whether the store is active.
     */
    private Boolean isActive;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;
}
