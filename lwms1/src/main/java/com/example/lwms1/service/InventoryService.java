package com.example.lwms1.service;

import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.InventoryRepository;
import com.example.lwms1.repository.SpaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository repo;
    private final SpaceRepository spaceRepo;

    public InventoryService(InventoryRepository repo, SpaceRepository spaceRepo) {
        this.repo = repo;
        this.spaceRepo = spaceRepo;
    }

    public List<Inventory> listAll() { return repo.findAll(); }

    @Transactional
    public void delete(Integer id) {
        Inventory inv = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        if (inv.getLocation() != null) {
            spaceRepo.findByZone(inv.getLocation()).ifPresent(space -> {
                updateSpaceCapacity(space, -inv.getQuantity());
            });
        }
        repo.delete(inv);
    }

    @Transactional
    public Inventory create(InventoryDTO dto) {
        if (dto.getLocation() == null || dto.getLocation().isEmpty()) {
            throw new BusinessException("Storage location is required.");
        }

        Integer spaceId = Integer.parseInt(dto.getLocation());
        Space space = spaceRepo.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Target Space not found"));

        if (space.getAvailableCapacity() < dto.getQuantity()) {
            throw new BusinessException("Insufficient space in " + space.getZone());
        }

        Inventory inv = new Inventory();
        inv.setItemName(dto.getItemName());
        inv.setCategory(dto.getCategory());
        inv.setQuantity(dto.getQuantity());
        inv.setLocation(space.getZone());
        inv.setLastUpdated(LocalDateTime.now());

        updateSpaceCapacity(space, dto.getQuantity());
        return repo.save(inv);
    }

    // THIS IS THE MISSING METHOD THAT WAS CAUSING YOUR COMPILE ERROR
    @Transactional
    public Inventory update(Integer id, InventoryDTO dto) {
        Inventory inv = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + id));

        Space space = spaceRepo.findByZone(inv.getLocation())
                .orElseThrow(() -> new ResourceNotFoundException("Associated Space not found for: " + inv.getLocation()));

        // Calculate if we are adding or removing quantity
        // Example: If old qty was 10 and new is 15, adjustment is +5 (uses more space)
        // If old was 10 and new is 8, adjustment is -2 (frees space)
        int capacityAdjustment = dto.getQuantity() - inv.getQuantity();

        if (space.getAvailableCapacity() < capacityAdjustment) {
            throw new BusinessException("Not enough room for this update in " + space.getZone());
        }

        inv.setItemName(dto.getItemName());
        inv.setCategory(dto.getCategory());
        inv.setQuantity(dto.getQuantity());
        inv.setLastUpdated(LocalDateTime.now());

        updateSpaceCapacity(space, capacityAdjustment);

        return repo.save(inv);
    }

    private void updateSpaceCapacity(Space space, int quantityChange) {
        int currentUsed = (space.getUsedCapacity() == null) ? 0 : space.getUsedCapacity();
        int total = (space.getTotalCapacity() == null) ? 0 : space.getTotalCapacity();

        int newUsed = currentUsed + quantityChange;
        space.setUsedCapacity(Math.max(0, newUsed));
        space.setAvailableCapacity(total - space.getUsedCapacity());

        spaceRepo.save(space);
    }
}