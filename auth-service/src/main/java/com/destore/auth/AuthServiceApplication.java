package com.destore.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @file AuthServiceApplication.java
 * @brief Main application class for DE-Store Auth Service
 * 
 * Provides JWT-based authentication and authorization for the DE-Store
 * microservices architecture. Supports role-based access control (RBAC)
 * with Network Manager and Store Manager roles.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
