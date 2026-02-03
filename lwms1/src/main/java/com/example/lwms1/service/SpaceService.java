package com.example.lwms1.service;

import com.example.lwms1.dto.SpaceAllocationDTO;
import com.example.lwms1.dto.SpaceDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SpaceService {

    private final SpaceRepository repo;

    @Autowired
    public SpaceService(SpaceRepository repo) {
        this.repo = repo;
    }

    public List<Space> listAll() {
        return repo.findAll();
    }

    public Space getById(Integer id) {
        Optional<Space> opt = repo.findById(id);
        if (opt.isEmpty()) {
            throw new ResourceNotFoundException("Space not found with id: " + id);
        }
        return opt.get();
    }

    @Transactional
    public Space create(SpaceDTO dto) {
        Space s = new Space();
        s.setZone(dto.getZone());

        // Simple validation for capacity
        int total = 1;
        if (dto.getTotalCapacity() != null && dto.getTotalCapacity() > 0) {
            total = dto.getTotalCapacity();
        }

        s.setTotalCapacity(total);
        s.setUsedCapacity(0); // New zones are empty
        s.setAvailableCapacity(total);

        return repo.save(s);
    }

    @Transactional
    public Space update(Integer id, SpaceDTO dto) {
        Space s = getById(id);

        s.setZone(dto.getZone());
        s.setTotalCapacity(dto.getTotalCapacity());

        // Math: Total - Used = Available
        int available = s.getTotalCapacity() - s.getUsedCapacity();

        // Safety Check: Don't allow total capacity to be less than current stock
        if (available < 0) {
            throw new BusinessException("Cannot reduce total capacity below current stock levels!");
        }

        s.setAvailableCapacity(available);
        return repo.save(s);
    }

    @Transactional
    public void delete(Integer id) {
        Space s = getById(id);

        // Business Rule: Can't delete a zone that has items in it
        if (s.getUsedCapacity() > 0) {
            throw new BusinessException("Zone " + s.getZone() + " is not empty. Remove items first.");
        }

        repo.delete(s);
    }

    @Transactional
    public Space allocate(Integer id, SpaceAllocationDTO dto) {
        Space s = getById(id);

        int newUsed = s.getUsedCapacity() + dto.getAmount();

        // Check if we have room
        if (newUsed > s.getTotalCapacity()) {
            throw new BusinessException("Insufficient space in " + s.getZone());
        }

        s.setUsedCapacity(newUsed);
        s.setAvailableCapacity(s.getTotalCapacity() - newUsed);
        return repo.save(s);
    }

    @Transactional
    public Space free(Integer id, SpaceAllocationDTO dto) {
        Space s = getById(id);

        int newUsed = s.getUsedCapacity() - dto.getAmount();

        // Safety: Capacity can't be negative
        if (newUsed < 0) {
            newUsed = 0;
        }

        s.setUsedCapacity(newUsed);
        s.setAvailableCapacity(s.getTotalCapacity() - newUsed);
        return repo.save(s);
    }
}