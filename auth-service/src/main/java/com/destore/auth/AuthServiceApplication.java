package com.destore.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for DE-Store Auth Service.
 * <p>
 * Provides JWT-based authentication and authorization for the DE-Store
 * microservices architecture. Supports role-based access control (RBAC)
 * with Network Manager and Store Manager roles.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
