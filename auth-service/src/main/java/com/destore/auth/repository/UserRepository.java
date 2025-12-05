package com.destore.auth.repository;

import com.destore.auth.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for User entity.
 * <p>
 * Provides database access methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds user by username.
     *
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds active user by username.
     *
     * @param username The username to search for
     * @return Optional containing the user if found and active
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);
}
