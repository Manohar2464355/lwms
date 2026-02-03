package com.example.lwms1.service;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.InventoryRepository;
import com.example.lwms1.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceTest {

    @Mock private ShipmentRepository shipmentRepo;
    @Mock private InventoryRepository inventoryRepo;

    @InjectMocks
    private ShipmentService shipmentService;

    private Inventory mockInventory;
    private Space mockSpace;
    private ShipmentDTO mockDto;

    @BeforeEach
    void setUp() {
        mockSpace = new Space();
        mockSpace.setTotalCapacity(100);
        mockSpace.setUsedCapacity(50);
        mockSpace.setAvailableCapacity(50);

        mockInventory = new Inventory();
        mockInventory.setItemId(1);
        mockInventory.setQuantity(20);
        mockInventory.setStorageSpace(mockSpace);

        mockDto = new ShipmentDTO();
        mockDto.setItemId(1);
        mockDto.setQuantity(10);
        mockDto.setOrigin("Warehouse A");
        mockDto.setDestination("Client X");
    }

    @Test
    @DisplayName("Receive (Dispatch): Should reduce inventory and update space")
    void testReceiveSuccess() {
        // Arrange
        when(inventoryRepo.findById(1)).thenReturn(Optional.of(mockInventory));

        // Act
        shipmentService.receive(mockDto);

        // Assert
        // 1. Inventory check: 20 - 10 = 10
        assertEquals(10, mockInventory.getQuantity());

        // 2. Space check: 50 used - 10 shipped = 40 used
        assertEquals(40, mockSpace.getUsedCapacity());
        assertEquals(60, mockSpace.getAvailableCapacity());

        verify(inventoryRepo).save(mockInventory);
        verify(shipmentRepo).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Receive (Dispatch): Should throw error if stock is insufficient")
    void testReceiveInsufficientStock() {
        // Arrange
        mockDto.setQuantity(50); // We only have 20 in mockInventory
        when(inventoryRepo.findById(1)).thenReturn(Optional.of(mockInventory));

        // Act & Assert
        Exception ex = assertThrows(RuntimeException.class, () -> shipmentService.receive(mockDto));
        assertTrue(ex.getMessage().contains("Insufficient stock"));

        verify(shipmentRepo, never()).save(any());
    }

    @Test
    @DisplayName("Delete: Should roll back space capacity when shipment is canceled")
    void testDeleteRollbackSpace() {
        // Arrange
        Shipment s = new Shipment();
        s.setShipmentId(100);
        s.setQuantity(10);
        s.setInventory(mockInventory);

        when(shipmentRepo.findById(100)).thenReturn(Optional.of(s));

        // Act
        shipmentService.delete(100);

        // Assert
        // Initial used was 50. Deleting shipment of 10 "adds it back" to space usage
        assertEquals(60, mockSpace.getUsedCapacity());
        verify(shipmentRepo).delete(s);
    }

    @Test
    @DisplayName("Update Status: Should change status correctly")
    void testUpdateStatus() {
        // Arrange
        Shipment s = new Shipment();
        s.setStatus("DISPATCHED");
        when(shipmentRepo.findById(100)).thenReturn(Optional.of(s));

        // Act
        shipmentService.updateStatus(100, "DELIVERED");

        // Assert
        assertEquals("DELIVERED", s.getStatus());
        verify(shipmentRepo).save(s);
    }
}