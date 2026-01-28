package com.example.lwms1.service;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceScheduleRepository repo;

    public MaintenanceService(MaintenanceScheduleRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> listAll() { return repo.findAll(); }

    public MaintenanceSchedule schedule(MaintenanceDTO dto) {
        MaintenanceSchedule m = new MaintenanceSchedule();
        m.setEquipmentId(dto.getEquipmentId());
        m.setDescription(dto.getDescription());
        m.setScheduledDate(dto.getScheduledDate());
        m.setCompletionStatus(dto.getCompletionStatus());
        return repo.save(m);
    }

    public MaintenanceSchedule update(Integer id, MaintenanceDTO dto) {
        MaintenanceSchedule m = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found: " + id));
        m.setEquipmentId(dto.getEquipmentId());
        m.setDescription(dto.getDescription());
        m.setScheduledDate(dto.getScheduledDate());
        m.setCompletionStatus(dto.getCompletionStatus());
        return repo.save(m);
    }

    public void delete(Integer id) {
        if (!repo.existsById(id))
            throw new ResourceNotFoundException("Maintenance not found: " + id);
        repo.deleteById(id);
    }
}