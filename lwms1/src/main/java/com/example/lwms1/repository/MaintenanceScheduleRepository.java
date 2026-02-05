package com.example.lwms1.repository;

import com.example.lwms1.model.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Integer> {

    boolean existsByEquipmentIdAndCompletionStatusIgnoreCase(Integer equipmentId, String status);

    long countByCompletionStatusIgnoreCase(String status);
}