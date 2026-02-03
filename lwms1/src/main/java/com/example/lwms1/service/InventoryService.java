package com.example.lwms1.service;

import com.example.lwms1.controller.InventoryController;
import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.InventoryRepository;
import com.example.lwms1.repository.SpaceRepository;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    public InventoryService(InventoryRepository repo,
                            SpaceRepository spaceRepo,
                            MaintenanceScheduleRepository maintenanceRepo) {
        this.repo = repo;
        this.spaceRepo = spaceRepo;
        this.maintenanceRepo = maintenanceRepo;
    }

    private void verifySpaceIsNotUnderMaintenance(Integer spaceId, String zoneName) {
        boolean isLocked = maintenanceRepo.existsByEquipmentIdAndCompletionStatusIgnoreCase(spaceId, "PENDING");
        if (isLocked) {
            logger.warn("Account is locked",isLocked);
            throw new BusinessException("Action Denied: Zone " + zoneName + " is currently under maintenance.");
        }
    }

    public Inventory findById(Integer id) {
        Optional<Inventory> itemOpt = repo.findById(id);
        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Item not found with ID: " + id);
        }
        return itemOpt.get();
    }


    public List<Inventory> listAll() {
        return repo.findAll();
    }

    @Transactional
    public void delete(Integer id) {
        Inventory inv = findById(id);

        if (inv.getLocation() != null) {
            Optional<Space> spaceOpt = spaceRepo.findByZone(inv.getLocation());
            if (spaceOpt.isEmpty()) {
                throw new ResourceNotFoundException("Space not found");
            }
            Space space = spaceOpt.get();

            verifySpaceIsNotUnderMaintenance(space.getSpaceId(), space.getZone());
            updateSpaceCapacity(space, -inv.getQuantity());
        }
        repo.delete(inv);
    }

    @Transactional
    public Inventory create(InventoryDTO dto) {
        if (dto.getLocation() == null || dto.getLocation().isEmpty()) {
            throw new BusinessException("Storage location is required.");
        }
//optional is a class in java 1.8 to prevent null pointer exception here and there are few methods like is empty and is preset()
        //isEmpty()-> to check data is empty or not and isPresent(0->is data present yes or not
        Optional<Space> spaceOpt = spaceRepo.findByZone(dto.getLocation());
        if (spaceOpt.isEmpty()) {
            throw new ResourceNotFoundException("Target Space '" + dto.getLocation() + "' not found");
        }
        Space space = spaceOpt.get();

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

        Optional<Space> spaceOpt = spaceRepo.findByZone(inv.getLocation());
        if (spaceOpt.isEmpty()) {
            throw new ResourceNotFoundException("Space not found");
        }
        Space space = spaceOpt.get();

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

    private void updateSpaceCapacity(Space space, int quantityChange) {
        int currentUsed = 0;
        if (space.getUsedCapacity() != null) {
            currentUsed = space.getUsedCapacity();
        }

        int total = 0;
        if (space.getTotalCapacity() != null) {
            total = space.getTotalCapacity();
        }

        int newUsed = currentUsed + quantityChange;

        if (newUsed < 0) {
            newUsed = 0;
        }

        space.setUsedCapacity(newUsed);
        space.setAvailableCapacity(total - newUsed);

        spaceRepo.save(space);
    }
}