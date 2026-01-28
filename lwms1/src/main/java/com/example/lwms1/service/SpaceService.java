package com.example.lwms1.service;

import com.example.lwms1.dto.SpaceAllocationDTO;
import com.example.lwms1.dto.SpaceDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.SpaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SpaceService {

    private final SpaceRepository repo;

    public SpaceService(SpaceRepository repo) {
        this.repo = repo;
    }

    public List<Space> listAll() {
        return repo.findAll();
    }

    @Transactional
    public Space create(SpaceDTO dto) {
        Space s = new Space();
        s.setZone(dto.getZone());

        // 1. Automation: Set Total Capacity (Default to 1 if empty to avoid UI errors)
        int total = (dto.getTotalCapacity() == null || dto.getTotalCapacity() <= 0) ? 1 : dto.getTotalCapacity();
        s.setTotalCapacity(total);

        // 2. Automation: Brand new zones MUST start empty (0 used, all available)
        s.setUsedCapacity(0);
        s.setAvailableCapacity(total);

        return repo.save(s);
    }

    @Transactional
    public Space update(Integer id, SpaceDTO dto) {
        Space s = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Space not found: " + id));

        s.setZone(dto.getZone());
        s.setTotalCapacity(dto.getTotalCapacity());

        // Recalculate availability based on existing usage
        int available = s.getTotalCapacity() - s.getUsedCapacity();
        if (available < 0) {
            throw new BusinessException("Update failed: New total capacity is less than current inventory usage!");
        }

        s.setAvailableCapacity(available);
        return repo.save(s);
    }

    public void delete(Integer id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Space not found: " + id);
        repo.deleteById(id);
    }

    @Transactional
    public Space allocate(Integer id, SpaceAllocationDTO dto) {
        Space s = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Space not found: " + id));
        int newUsed = s.getUsedCapacity() + dto.getAmount();

        if (newUsed > s.getTotalCapacity()) {
            throw new BusinessException("Insufficient available capacity in " + s.getZone());
        }

        s.setUsedCapacity(newUsed);
        s.setAvailableCapacity(s.getTotalCapacity() - newUsed);
        return repo.save(s);
    }

    @Transactional
    public Space free(Integer id, SpaceAllocationDTO dto) {
        Space s = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Space not found: " + id));
        int newUsed = s.getUsedCapacity() - dto.getAmount();

        if (newUsed < 0) {
            throw new BusinessException("Cannot free more than currently used in " + s.getZone());
        }

        s.setUsedCapacity(newUsed);
        s.setAvailableCapacity(s.getTotalCapacity() - newUsed);
        return repo.save(s);
    }
}