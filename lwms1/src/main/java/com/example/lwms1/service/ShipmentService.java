package com.example.lwms1.service;

import com.example.lwms1.dto.ShipmentDTO;
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

    public List<Shipment> listAll() {
        return shipmentRepo.findAll();
    }

    @Transactional
    public void receive(ShipmentDTO dto) {
        // 1. Find the Item
        Optional<Inventory> invOpt = inventoryRepo.findById(dto.getItemId());
        if (invOpt.isEmpty()) {
            throw new ResourceNotFoundException("Item not found");
        }
        Inventory inv = invOpt.get();

        // 2. Validate Stock
        if (inv.getQuantity() < dto.getQuantity()) {
            throw new RuntimeException("Insufficient stock available.");
        }

        // 3. Subtract from Inventory
        inv.setQuantity(inv.getQuantity() - dto.getQuantity());

        // 4. Update Warehouse Space Math
        if (inv.getStorageSpace() != null) {
            Space space = inv.getStorageSpace();
            int used = 0;
            if (space.getUsedCapacity() != null) {
                used = space.getUsedCapacity();
            }

            // Reducing stock reduces used space
            int newUsed = used - dto.getQuantity();
            if (newUsed < 0) newUsed = 0;

            space.setUsedCapacity(newUsed);
            space.setAvailableCapacity(space.getTotalCapacity() - newUsed);
        }

        // 5. Create Shipment Record
        Shipment s = new Shipment();
        s.setInventory(inv);
        s.setQuantity(dto.getQuantity());
        s.setOrigin(dto.getOrigin());
        s.setDestination(dto.getDestination());
        s.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());

        // Set status from DTO or default to PENDING
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            s.setStatus(dto.getStatus().toUpperCase());
        } else {
            s.setStatus("PENDING");
        }

        inventoryRepo.save(inv);
        shipmentRepo.save(s);
    }

    @Transactional
    public void updateFullShipment(ShipmentDTO dto) {
        Optional<Shipment> shipOpt = shipmentRepo.findById(dto.getShipmentId());
        if (shipOpt.isEmpty()) {
            throw new ResourceNotFoundException("Shipment not found");
        }
        Shipment s = shipOpt.get();

        Optional<Inventory> invOpt = inventoryRepo.findById(dto.getItemId());
        if (invOpt.isEmpty()) {
            throw new ResourceNotFoundException("Item not found");
        }

        s.setInventory(invOpt.get());
        s.setOrigin(dto.getOrigin());
        s.setDestination(dto.getDestination());
        s.setStatus(dto.getStatus());
        s.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());

        shipmentRepo.save(s);
    }

    public Shipment get(Integer id) {
        Optional<Shipment> s = shipmentRepo.findById(id);
        if (s.isEmpty()) {
            throw new ResourceNotFoundException("Shipment not found");
        }
        return s.get();
    }

    @Transactional
    public void delete(Integer id) {
        Optional<Shipment> shipOpt = shipmentRepo.findById(id);
        if (shipOpt.isEmpty()) {
            throw new ResourceNotFoundException("Shipment not found");
        }

        Shipment s = shipOpt.get();

        // Optional: Re-calculate space if the shipment is canceled/deleted
        Inventory inv = s.getInventory();
        if (inv != null && inv.getStorageSpace() != null) {
            Space space = inv.getStorageSpace();
            int restoredUsed = space.getUsedCapacity() + s.getQuantity();

            space.setUsedCapacity(restoredUsed);
            space.setAvailableCapacity(space.getTotalCapacity() - restoredUsed);
        }

        shipmentRepo.delete(s);
    }
}