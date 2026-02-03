package com.example.lwms1.repository;

import com.example.lwms1.model.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Integer> {

    // This allows the Service to check if a specific zone is locked
    boolean existsByEquipmentIdAndCompletionStatusIgnoreCase(Integer equipmentId, String status);

    // This fixes the "error" you are seeing in the Service
    long countByCompletionStatusIgnoreCase(String status);
}