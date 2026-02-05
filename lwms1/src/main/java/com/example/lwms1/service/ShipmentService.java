package com.example.lwms1.service;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.InventoryRepository;
import com.example.lwms1.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepo;
    private final InventoryRepository inventoryRepo;

    @Autowired
    public ShipmentService(ShipmentRepository shipmentRepo, InventoryRepository inventoryRepo) {
        this.shipmentRepo = shipmentRepo;
        this.inventoryRepo = inventoryRepo;
    }

    @Transactional(readOnly = true)
    public List<Shipment> listAll() {
        return shipmentRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Shipment get(Integer id) {
        Optional<Shipment> shipmentOptional = shipmentRepo.findById(id);
        if (shipmentOptional.isPresent()) {
            return shipmentOptional.get();
        } else {
            throw new ResourceNotFoundException("Shipment not found ID: " + id);
        }
    }

    @Transactional(readOnly = true)
    public ShipmentDTO getDtoById(Integer id) {
        Shipment s = get(id);
        ShipmentDTO dto = new ShipmentDTO();
        dto.setShipmentId(s.getShipmentId());
        dto.setOrigin(s.getOrigin());
        dto.setDestination(s.getDestination());
        dto.setStatus(s.getStatus());
        dto.setExpectedDeliveryDate(s.getExpectedDeliveryDate());
        dto.setQuantity(s.getQuantity());

        if (s.getInventory() != null) {
            dto.setItemId(s.getInventory().getItemId());
        }
        return dto;
    }

    @Transactional
    public void create(ShipmentDTO dto) {
        Optional<Inventory> inventoryOptional = inventoryRepo.findById(dto.getItemId());
        Inventory inv;
        if (inventoryOptional.isPresent()) {
            inv = inventoryOptional.get();
        } else {
            throw new ResourceNotFoundException("Item not found");
        }
        if (inv.getQuantity() < dto.getQuantity()) {
            throw new BusinessException("Insufficient stock available.");
        }
        inv.setQuantity(inv.getQuantity() - dto.getQuantity());
        if (inv.getStorageSpace() != null) {
            updateSpaceCapacity(inv.getStorageSpace(), -dto.getQuantity());
        }
        Shipment s = new Shipment();
        s.setInventory(inv);
        s.setQuantity(dto.getQuantity());
        s.setOrigin(dto.getOrigin());
        s.setDestination(dto.getDestination());
        s.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        s.setStatus(dto.getStatus() != null ? dto.getStatus().toUpperCase() : "PENDING");
        inventoryRepo.save(inv);
        shipmentRepo.save(s);
    }

    @Transactional
    public void updateFullShipment(ShipmentDTO dto) {
        Shipment s = get(dto.getShipmentId());
        Optional<Inventory> inventoryOptional = inventoryRepo.findById(dto.getItemId());
        Inventory inv;
        if (inventoryOptional.isPresent()) {
            inv = inventoryOptional.get();
        } else {
            throw new ResourceNotFoundException("Item not found");
        }
        s.setInventory(inv);
        s.setOrigin(dto.getOrigin());
        s.setDestination(dto.getDestination());
        s.setStatus(dto.getStatus());
        s.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        shipmentRepo.save(s);
    }

    @Transactional
    public void delete(Integer id) {
        Shipment s = get(id);
        Inventory inv = s.getInventory();
        if (inv != null) {
            inv.setQuantity(inv.getQuantity() + s.getQuantity());
            if (inv.getStorageSpace() != null) {
                updateSpaceCapacity(inv.getStorageSpace(), s.getQuantity());
            }
            inventoryRepo.save(inv);
        }
        shipmentRepo.delete(s);
    }

    private void updateSpaceCapacity(Space space, int change) {
        int currentUsed = space.getUsedCapacity() != null ? space.getUsedCapacity() : 0;
        int newUsed = currentUsed + change;
        if (newUsed < 0) newUsed = 0;

        space.setUsedCapacity(newUsed);
        space.setAvailableCapacity(space.getTotalCapacity() - newUsed);
    }
}