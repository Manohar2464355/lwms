package com.example.lwms1.service;
import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.InventoryRepository;
import com.example.lwms1.repository.SpaceRepository;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    private final InventoryRepository repo;
    private final SpaceRepository spaceRepo;
    private final MaintenanceScheduleRepository maintenanceRepo;

    @Autowired
    public InventoryService(InventoryRepository repo, SpaceRepository spaceRepo, MaintenanceScheduleRepository maintenanceRepo) {
        this.repo = repo;
        this.spaceRepo = spaceRepo;
        this.maintenanceRepo = maintenanceRepo;
    }

    @Transactional(readOnly = true)
    public List<Inventory> listAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Inventory findById(Integer id) {
        Optional<Inventory> result = repo.findById(id);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new ResourceNotFoundException("Item not found ID: " + id);
        }
    }

    @Transactional(readOnly = true)
    public InventoryDTO getDtoById(Integer id) {
        Inventory item = findById(id);
        InventoryDTO dto = new InventoryDTO();
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setCategory(item.getCategory());
        dto.setQuantity(item.getQuantity());
        dto.setLocation(item.getLocation());
        return dto;
    }

    @Transactional
    public Inventory create(InventoryDTO dto) {
        Optional<Space> spaceOptional = spaceRepo.findByZone(dto.getLocation());
        Space space;
        if (spaceOptional.isPresent()) {
            space = spaceOptional.get();
        } else {
            throw new ResourceNotFoundException("Space not found: " + dto.getLocation());
        }
        verifySpaceIsNotUnderMaintenance(space.getSpaceId(), space.getZone());
        if (space.getAvailableCapacity() < dto.getQuantity()) {
            throw new BusinessException("Insufficient space in " + space.getZone());
        }

        Inventory inv = new Inventory();
        inv.setItemName(dto.getItemName());
        inv.setCategory(dto.getCategory());
        inv.setQuantity(dto.getQuantity());
        inv.setLocation(space.getZone());
        inv.setStorageSpace(space);
        inv.setLastUpdated(LocalDateTime.now());

        updateSpaceCapacity(space, dto.getQuantity());
        return repo.save(inv);
    }

    @Transactional
    public Inventory update(Integer id, InventoryDTO dto) {
        Inventory inv = findById(id);
        Optional<Space> spaceOptional = spaceRepo.findByZone(inv.getLocation());
        Space space;
        if (spaceOptional.isPresent()) {
            space = spaceOptional.get();
        } else {
            throw new ResourceNotFoundException("Space not found");
        }

        verifySpaceIsNotUnderMaintenance(space.getSpaceId(), space.getZone());
        int capacityAdjustment = dto.getQuantity() - inv.getQuantity();

        if (space.getAvailableCapacity() < capacityAdjustment) {
            throw new BusinessException("Not enough room in " + space.getZone());
        }

        inv.setItemName(dto.getItemName());
        inv.setCategory(dto.getCategory());
        inv.setQuantity(dto.getQuantity());
        inv.setLastUpdated(LocalDateTime.now());
        updateSpaceCapacity(space, capacityAdjustment);
        return repo.save(inv);
    }
    @Transactional
    public void delete(Integer id) {
        Inventory inv = findById(id);
        if (inv.getLocation() != null) {
            Space space = spaceRepo.findByZone(inv.getLocation()).orElse(null);
            if (space != null) {
                verifySpaceIsNotUnderMaintenance(space.getSpaceId(), space.getZone());
                updateSpaceCapacity(space, -inv.getQuantity()); // Refund space
            }
        }
        repo.delete(inv);
    }

    private void verifySpaceIsNotUnderMaintenance(Integer spaceId, String zoneName) {
        boolean isLocked = maintenanceRepo.existsByEquipmentIdAndCompletionStatusIgnoreCase(spaceId, "PENDING");
        if (isLocked) {
            throw new BusinessException("Action Denied: Zone " + zoneName + " is under maintenance.");
        }
    }

    private void updateSpaceCapacity(Space space, int quantityChange) {
        int newUsed = (space.getUsedCapacity() != null ? space.getUsedCapacity() : 0) + quantityChange;
        if (newUsed < 0) newUsed = 0;

        space.setUsedCapacity(newUsed);
        space.setAvailableCapacity(space.getTotalCapacity() - newUsed);
        spaceRepo.save(space);
    }
}