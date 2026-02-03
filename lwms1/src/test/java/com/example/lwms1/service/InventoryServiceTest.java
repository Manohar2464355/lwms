package com.example.lwms1.service;

import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Space;
import com.example.lwms1.repository.InventoryRepository;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import com.example.lwms1.repository.SpaceRepository;
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
public class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepo;
    @Mock private SpaceRepository spaceRepo;
    @Mock private MaintenanceScheduleRepository maintenanceRepo;

    @InjectMocks
    private InventoryService inventoryService;

    private Space mockSpace;
    private InventoryDTO mockDto;

    @BeforeEach
    void setUp() {
        mockSpace = new Space();
        mockSpace.setSpaceId(10);
        mockSpace.setZone("Zone-A");
        mockSpace.setTotalCapacity(100);
        mockSpace.setUsedCapacity(10);
        mockSpace.setAvailableCapacity(90);

        mockDto = new InventoryDTO();
        mockDto.setItemName("Laptop");
        mockDto.setCategory("Electronics");
        mockDto.setQuantity(5);
        mockDto.setLocation("Zone-A");
    }

    @Test
    @DisplayName("Create: Should succeed when space is available and not under maintenance")
    void testCreateInventorySuccess() {
        // Arrange
        when(spaceRepo.findByZone("Zone-A")).thenReturn(Optional.of(mockSpace));
        when(maintenanceRepo.existsByEquipmentIdAndCompletionStatusIgnoreCase(10, "PENDING")).thenReturn(false);
        when(inventoryRepo.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Inventory result = inventoryService.create(mockDto);

        // Assert
        assertNotNull(result);
        assertEquals(15, mockSpace.getUsedCapacity()); // 10 initial + 5 new
        assertEquals(85, mockSpace.getAvailableCapacity());
        verify(inventoryRepo).save(any(Inventory.class));
        verify(spaceRepo).save(mockSpace);
    }

    @Test
    @DisplayName("Maintenance Check: Should block creation if zone is under maintenance")
    void testCreateFailsDuringMaintenance() {
        // Arrange
        when(spaceRepo.findByZone("Zone-A")).thenReturn(Optional.of(mockSpace));
        // Simulate a "PENDING" maintenance task for this space
        when(maintenanceRepo.existsByEquipmentIdAndCompletionStatusIgnoreCase(10, "PENDING")).thenReturn(true);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> inventoryService.create(mockDto));
        assertTrue(ex.getMessage().contains("under maintenance"));

        verify(inventoryRepo, never()).save(any());
    }

    @Test
    @DisplayName("Capacity Check: Should throw exception if not enough space")
    void testCreateFailsInsufficientSpace() {
        // Arrange
        mockDto.setQuantity(200); // Exceeds total capacity of 100
        when(spaceRepo.findByZone("Zone-A")).thenReturn(Optional.of(mockSpace));
        when(maintenanceRepo.existsByEquipmentIdAndCompletionStatusIgnoreCase(10, "PENDING")).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> inventoryService.create(mockDto));
    }

    @Test
    @DisplayName("Update: Should adjust capacity correctly when quantity changes")
    void testUpdateInventoryAdjustment() {
        // Arrange
        Inventory existingInv = new Inventory();
        existingInv.setQuantity(10); // Current quantity in DB
        existingInv.setLocation("Zone-A");

        mockDto.setQuantity(15); // Updating to 15 (Adjustment of +5)

        when(inventoryRepo.findById(1)).thenReturn(Optional.of(existingInv));
        when(spaceRepo.findByZone("Zone-A")).thenReturn(Optional.of(mockSpace));
        when(maintenanceRepo.existsByEquipmentIdAndCompletionStatusIgnoreCase(10, "PENDING")).thenReturn(false);

        // Act
        inventoryService.update(1, mockDto);

        // Assert
        // Initial Space Used was 10. Adjustment is +5. New Used = 15.
        assertEquals(15, mockSpace.getUsedCapacity());
        verify(spaceRepo).save(mockSpace);
    }

    @Test
    @DisplayName("Delete: Should increase available capacity when item is removed")
    void testDeleteFreesSpace() {
        // Arrange
        Inventory existingInv = new Inventory();
        existingInv.setQuantity(10);
        existingInv.setLocation("Zone-A");

        when(inventoryRepo.findById(1)).thenReturn(Optional.of(existingInv));
        when(spaceRepo.findByZone("Zone-A")).thenReturn(Optional.of(mockSpace));
        when(maintenanceRepo.existsByEquipmentIdAndCompletionStatusIgnoreCase(10, "PENDING")).thenReturn(false);

        // Act
        inventoryService.delete(1);

        // Assert
        // Initial Space Used was 10. Delete 10. New Used = 0.
        assertEquals(0, mockSpace.getUsedCapacity());
        assertEquals(100, mockSpace.getAvailableCapacity());
        verify(inventoryRepo).delete(existingInv);
    }
}