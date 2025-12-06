package com.destore.store.repository;

import com.destore.store.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @file StoreRepository.java
 * @brief Repository interface for Store entity
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {

    Optional<Store> findByStoreCode(String storeCode);
    
    List<Store> findByIsActive(Boolean isActive);
    
    boolean existsByStoreCode(String storeCode);
}
