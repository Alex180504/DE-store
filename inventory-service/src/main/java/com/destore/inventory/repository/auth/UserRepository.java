package com.destore.inventory.repository.auth;

import com.destore.inventory.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for auth users - READ ONLY.
 * <p>
 * Provides access to User entities in the authentication database.
 * Used primarily to fetch network manager emails.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds all active users with the NETWORK_MANAGER role.
     *
     * @return a list of active network managers
     */
    @Query("SELECT u FROM User u WHERE u.role = 'NETWORK_MANAGER' AND u.isActive = true")
    List<User> findActiveNetworkManagers();
}
