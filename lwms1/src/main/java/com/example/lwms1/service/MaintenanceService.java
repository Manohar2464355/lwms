package com.example.lwms1.service;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        // Simple logic for default status
        if (dto.getCompletionStatus() != null) {
            m.setCompletionStatus(dto.getCompletionStatus());
        } else {
            m.setCompletionStatus("PENDING");
        }

        return repo.save(m);
    }

    /**
     * Logic to flip the status.
     * Explain this as a "Light Switch": PENDING means Locked, COMPLETED means Unlocked.
     */
    public String toggleStatus(Integer id) {
        Optional<MaintenanceSchedule> opt = repo.findById(id);

        if (opt.isEmpty()) {
            throw new ResourceNotFoundException("Maintenance not found ID: " + id);
        }

        MaintenanceSchedule m = opt.get();
        String currentStatus = m.getCompletionStatus();

        // Classic if-else logic to flip the status
        String nextStatus;
        if ("PENDING".equalsIgnoreCase(currentStatus)) {
            nextStatus = "COMPLETED";
        } else {
            nextStatus = "PENDING";
        }

        m.setCompletionStatus(nextStatus);
        repo.save(m);
        return nextStatus;
    }

    public MaintenanceSchedule update(Integer id, MaintenanceDTO dto) {
        Optional<MaintenanceSchedule> opt = repo.findById(id);

        if (opt.isEmpty()) {
            throw new ResourceNotFoundException("Maintenance not found ID: " + id);
        }

        MaintenanceSchedule m = opt.get();
        m.setEquipmentId(dto.getEquipmentId());
        m.setDescription(dto.getDescription());
        m.setScheduledDate(dto.getScheduledDate());
        m.setCompletionStatus(dto.getCompletionStatus());

        return repo.save(m);
    }

    public void delete(Integer id) {
        // Direct check before deleting
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Maintenance not found ID: " + id);
        }
    }

    @Transactional(readOnly = true)
    public long getPendingMaintenanceCount() {
        // This is used for the Admin Dashboard count card
        return repo.countByCompletionStatusIgnoreCase("PENDING");
    }
}