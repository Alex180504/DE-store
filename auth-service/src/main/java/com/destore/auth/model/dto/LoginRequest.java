package com.destore.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Username for authentication.
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Password for authentication.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
