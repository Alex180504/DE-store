package com.destore.store.repository;

import com.destore.store.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Store entity.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {

    /**
     * Finds store by store code.
     *
     * @param storeCode Store code
     * @return Optional containing store if found
     */
    Optional<Store> findByStoreCode(String storeCode);
    
    /**
     * Finds stores by active status.
     *
     * @param isActive Active status
     * @return List of stores
     */
    List<Store> findByIsActive(Boolean isActive);
    
    /**
     * Checks if store exists by store code.
     *
     * @param storeCode Store code
     * @return True if exists
     */
    boolean existsByStoreCode(String storeCode);
}
