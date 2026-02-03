package com.example.lwms1.service;

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

        int total = (dto.getTotalCapacity() != null && dto.getTotalCapacity() > 0) ? dto.getTotalCapacity() : 1;

        s.setTotalCapacity(total);
        s.setUsedCapacity(0);
        s.setAvailableCapacity(total);

        return repo.save(s);
    }

    @Transactional
    public Space update(Integer id, SpaceDTO dto) {
        Space s = getById(id);

        s.setZone(dto.getZone());
        s.setTotalCapacity(dto.getTotalCapacity());

        int available = s.getTotalCapacity() - s.getUsedCapacity();

        if (available < 0) {
            throw new BusinessException("Cannot reduce total capacity below current stock levels!");
        }

        s.setAvailableCapacity(available);
        return repo.save(s);
    }

    @Transactional
    public void delete(Integer id) {
        Space s = getById(id);

        if (s.getUsedCapacity() > 0) {
            throw new BusinessException("Zone " + s.getZone() + " is not empty. Remove items first.");
        }

        repo.delete(s);
    }
}