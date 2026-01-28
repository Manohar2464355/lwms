package com.example.lwms1.service;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.repository.InventoryRepository;
import com.example.lwms1.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepo;
    private final InventoryRepository inventoryRepo;

    public ShipmentService(ShipmentRepository shipmentRepo, InventoryRepository inventoryRepo) {
        this.shipmentRepo = shipmentRepo;
        this.inventoryRepo = inventoryRepo;
    }

    public List<Shipment> listAll() {
        return shipmentRepo.findAll();
    }

    @Transactional
    public void receive(ShipmentDTO dto) {
        Inventory inv = inventoryRepo.findById(dto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        Shipment s = new Shipment();
        s.setInventory(inv);
        s.setOrigin(dto.getOrigin());
        s.setDestination(dto.getDestination());
        s.setStatus(dto.getStatus() == null || dto.getStatus().isEmpty() ? "DISPATCHED" : dto.getStatus());
        s.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());

        inv.setQuantity((inv.getQuantity() == null ? 0 : inv.getQuantity()) + 1);
        inventoryRepo.save(inv);
        shipmentRepo.save(s);
    }

    // --- NEW UPDATE METHOD ---
    @Transactional
    public void updateFullShipment(ShipmentDTO dto) {
        Shipment s = shipmentRepo.findById(dto.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        Inventory inv = inventoryRepo.findById(dto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        s.setInventory(inv);
        s.setOrigin(dto.getOrigin());
        s.setDestination(dto.getDestination());
        s.setStatus(dto.getStatus());
        s.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());

        shipmentRepo.save(s);
    }


    public Shipment get(Integer id) {
        return shipmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
    }

    public void updateStatus(Integer id, String status) {
        Shipment s = get(id);
        s.setStatus(status);
        shipmentRepo.save(s);
    }

    public void delete(Integer id) {
        shipmentRepo.deleteById(id);
    }
}