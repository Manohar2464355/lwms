package com.example.lwms1.repository;

import com.example.lwms1.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Integer> {
    // Add this to find the space by name when deleting inventory
    Optional<Space> findByZone(String zone);
}