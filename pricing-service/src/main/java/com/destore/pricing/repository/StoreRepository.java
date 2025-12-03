package com.destore.pricing.repository;

import com.destore.pricing.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @file StoreRepository.java
 * @brief Spring Data JPA repository for Store entity
 * 
 * Provides CRUD operations and custom queries for store management.
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {

    /**
     * @brief Find a store by its unique store code
     * @param storeCode The store code (e.g., 'LON-001')
     * @return Optional containing the store if found
     */
    Optional<Store> findByStoreCode(String storeCode);

    /**
     * @brief Check if a store exists by store code
     * @param storeCode The store code to check
     * @return true if store exists
     */
    boolean existsByStoreCode(String storeCode);
}
