package com.destore.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @file LoginRequest.java
 * @brief DTO for user login requests
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * @brief Username for authentication
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * @brief Password for authentication
     */
    @NotBlank(message = "Password is required")
    private String password;
}
