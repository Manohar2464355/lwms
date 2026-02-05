package com.example.lwms1.service;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceScheduleRepository repo;

    @Autowired
    public MaintenanceService(MaintenanceScheduleRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> listAll() {
        return repo.findAll();
    }

    public MaintenanceSchedule schedule(MaintenanceDTO dto) {
        MaintenanceSchedule m = new MaintenanceSchedule();
        m.setEquipmentId(dto.getEquipmentId());
        m.setDescription(dto.getDescription());
        m.setScheduledDate(dto.getScheduledDate());
        if (dto.getCompletionStatus() != null) {
            m.setCompletionStatus(dto.getCompletionStatus());
        } else {
            m.setCompletionStatus("PENDING");
        }
        return repo.save(m);
    }

    public String toggleStatusAndGetMessage(Integer id) {
        Optional<MaintenanceSchedule> opt = repo.findById(id);
        if (opt.isEmpty()) {
            throw new ResourceNotFoundException("Maintenance not found ID: " + id);
        }
        MaintenanceSchedule m = opt.get();
        String currentStatus = m.getCompletionStatus();
        String nextStatus = "PENDING".equalsIgnoreCase(currentStatus) ? "COMPLETED" : "PENDING";
        m.setCompletionStatus(nextStatus);
        repo.save(m);
        if ("COMPLETED".equals(nextStatus)) {
            return "Maintenance finished. Zone is UNLOCKED.";
        } else {
            return "Maintenance reopened. Zone is LOCKED.";
        }
    }

    public void delete(Integer id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Maintenance not found ID: " + id);
        }
    }

    @Transactional(readOnly = true)
    public long getPendingMaintenanceCount() {
        return repo.countByCompletionStatusIgnoreCase("PENDING");
    }
    public List<Integer> getCurrentlyLockedSpaceIds() {
        List<MaintenanceSchedule> allSchedules = repo.findAll();
        List<Integer> lockedIds = new ArrayList<Integer>();
        for (MaintenanceSchedule schedule : allSchedules) {
            if ("PENDING".equalsIgnoreCase(schedule.getCompletionStatus())) {
                lockedIds.add(schedule.getEquipmentId());
            }
        }
        return lockedIds;
    }
}