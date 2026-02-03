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

    @Mock
    private ShipmentRepository shipmentRepo;

    @Mock
    private InventoryRepository inventoryRepo;

    @InjectMocks
    private ShipmentService shipmentService;

    private Inventory mockInventory;
    private Space mockSpace;
    private ShipmentDTO mockDto;

    @BeforeEach
    void setUp() {
        // Initialize Space
        mockSpace = new Space();
        mockSpace.setTotalCapacity(100);
        mockSpace.setUsedCapacity(50);
        mockSpace.setAvailableCapacity(50);

        // Initialize Inventory linked to Space
        mockInventory = new Inventory();
        mockInventory.setItemId(1);
        mockInventory.setQuantity(20);
        mockInventory.setStorageSpace(mockSpace);

        // Initialize DTO for testing
        mockDto = new ShipmentDTO();
        mockDto.setItemId(1);
        mockDto.setQuantity(10);
        mockDto.setOrigin("Warehouse A");
        mockDto.setDestination("Client X");
        mockDto.setStatus("PENDING");
    }

    @Test
    @DisplayName("Receive (Dispatch): Should reduce inventory and update space usage")
    void testReceiveSuccess() {
        // Arrange
        when(inventoryRepo.findById(1)).thenReturn(Optional.of(mockInventory));

        // Act
        shipmentService.receive(mockDto);

        // Assert
        // Inventory check: 20 starting - 10 shipped = 10 left
        assertEquals(10, mockInventory.getQuantity());

        // Space math: 50 used - 10 quantity = 40 new used capacity
        assertEquals(40, mockSpace.getUsedCapacity());
        assertEquals(60, mockSpace.getAvailableCapacity());

        verify(inventoryRepo).save(mockInventory);
        verify(shipmentRepo).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Receive (Dispatch): Should throw error if inventory stock is too low")
    void testReceiveInsufficientStock() {
        // Arrange
        mockDto.setQuantity(50); // Setting quantity higher than mockInventory (20)
        when(inventoryRepo.findById(1)).thenReturn(Optional.of(mockInventory));

        // Act & Assert
        Exception ex = assertThrows(RuntimeException.class, () -> shipmentService.receive(mockDto));
        assertTrue(ex.getMessage().contains("Insufficient stock"));

        verify(shipmentRepo, never()).save(any());
    }

    @Test
    @DisplayName("Update Full Shipment: Should update all shipment fields via DTO")
    void testUpdateFullShipmentSuccess() {
        // Arrange
        Shipment existingShipment = new Shipment();
        existingShipment.setShipmentId(100);
        existingShipment.setStatus("PENDING");

        mockDto.setShipmentId(100);
        mockDto.setStatus("DELIVERED");
        mockDto.setItemId(1);

        when(shipmentRepo.findById(100)).thenReturn(Optional.of(existingShipment));
        when(inventoryRepo.findById(1)).thenReturn(Optional.of(mockInventory));

        // Act
        shipmentService.updateFullShipment(mockDto);

        // Assert
        assertEquals("DELIVERED", existingShipment.getStatus());
        verify(shipmentRepo).save(existingShipment);
    }

    @Test
    @DisplayName("Delete: Should increase space capacity when shipment is deleted")
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
        // If we delete a shipment of 10, your service logic adds 10 back to UsedCapacity
        // Initial 50 + 10 = 60
        assertEquals(60, mockSpace.getUsedCapacity());
        verify(shipmentRepo).delete(s);
    }

    @Test
    @DisplayName("Get Shipment: Should throw ResourceNotFoundException for invalid ID")
    void testGetNotFound() {
        // Arrange
        when(shipmentRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> shipmentService.get(999));
    }
}