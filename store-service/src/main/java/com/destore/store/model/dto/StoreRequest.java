package com.destore.store.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating stores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

    /**
     * Store code (e.g., LON-001).
     */
    @NotBlank(message = "Store code is required")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{3}$", message = "Store code must be in format XXX-NNN (e.g., LON-001)")
    private String storeCode;

    /**
     * Store name.
     */
    @NotBlank(message = "Store name is required")
    @Size(max = 100, message = "Store name must not exceed 100 characters")
    private String storeName;

    /**
     * Store address.
     */
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    /**
     * Store postcode.
     */
    @NotBlank(message = "Postcode is required")
    @Size(max = 10, message = "Postcode must not exceed 10 characters")
    private String postcode;

    /**
     * Whether the store is active.
     */
    private Boolean isActive = true;
}
