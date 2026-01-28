package com.example.lwms1.repository;

import com.example.lwms1.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    // You likely already have this, but ensure it's there
    Optional<Inventory> findById(Integer itemId);
}