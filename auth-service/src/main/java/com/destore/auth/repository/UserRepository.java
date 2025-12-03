package com.destore.auth.repository;

import com.destore.auth.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @file UserRepository.java
 * @brief JPA Repository for User entity
 * 
 * Provides database access methods for user management
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * @brief Find user by username
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * @brief Find active user by username
     * @param username The username to search for
     * @return Optional containing the user if found and active
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);
}
