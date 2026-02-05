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

    @Transactional(readOnly = true)
    public List<Space> listAll() {
        return repo.findAll();
    }
    @Transactional(readOnly = true)
    public Space getById(Integer id) {
        Optional<Space> spaceOptional = repo.findById(id);
        if (spaceOptional.isPresent()) {
            return spaceOptional.get();
        } else {
            throw new ResourceNotFoundException("Space not found with id: " + id);
        }
    }


    @Transactional(readOnly = true)
    public SpaceDTO getDtoById(Integer id) {
        Space space = getById(id);
        SpaceDTO dto = new SpaceDTO();
        dto.setZone(space.getZone());
        dto.setTotalCapacity(space.getTotalCapacity());
        return dto;
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

        int available = s.getTotalCapacity() - (s.getUsedCapacity() != null ? s.getUsedCapacity() : 0);

        if (available < 0) {
            throw new BusinessException("Cannot reduce total capacity below current stock levels (" + s.getUsedCapacity() + ")!");
        }

        s.setAvailableCapacity(available);
        return repo.save(s);
    }

    @Transactional
    public void delete(Integer id) {
        Space s = getById(id);

        if (s.getUsedCapacity() != null && s.getUsedCapacity() > 0) {
            throw new BusinessException("Zone " + s.getZone() + " is not empty. Remove items first.");
        }

        repo.delete(s);
    }
}