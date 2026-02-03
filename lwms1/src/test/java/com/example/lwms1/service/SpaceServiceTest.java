package com.example.lwms1.service;

import com.example.lwms1.dto.SpaceAllocationDTO;
import com.example.lwms1.dto.SpaceDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Space;
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
public class SpaceServiceTest {

    @Mock
    private SpaceRepository repo;

    @InjectMocks
    private SpaceService spaceService;

    private Space mockSpace;

    @BeforeEach
    void setUp() {
        mockSpace = new Space();
        mockSpace.setSpaceId(1);
        mockSpace.setZone("Zone A");
        mockSpace.setTotalCapacity(100);
        mockSpace.setUsedCapacity(40);
        mockSpace.setAvailableCapacity(60);
    }

    @Test
    @DisplayName("Create: Should initialize new zone with 0 used capacity")
    void testCreateSpace() {
        SpaceDTO dto = new SpaceDTO();
        dto.setZone("Zone B");
        dto.setTotalCapacity(500);

        when(repo.save(any(Space.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Space saved = spaceService.create(dto);

        assertEquals(0, saved.getUsedCapacity());
        assertEquals(500, saved.getAvailableCapacity());
        verify(repo).save(any(Space.class));
    }

    @Test
    @DisplayName("Allocate: Should increase used capacity on success")
    void testAllocateSuccess() {
        SpaceAllocationDTO alloc = new SpaceAllocationDTO();
        alloc.setAmount(20);

        when(repo.findById(1)).thenReturn(Optional.of(mockSpace));
        when(repo.save(any(Space.class))).thenReturn(mockSpace);

        Space result = spaceService.allocate(1, alloc);

        assertEquals(60, result.getUsedCapacity());
        assertEquals(40, result.getAvailableCapacity());
    }

    @Test
    @DisplayName("Allocate: Should throw BusinessException when capacity exceeded")
    void testAllocateOverlimit() {
        SpaceAllocationDTO alloc = new SpaceAllocationDTO();
        alloc.setAmount(70); // 40 + 70 = 110 (Max 100)

        when(repo.findById(1)).thenReturn(Optional.of(mockSpace));

        assertThrows(BusinessException.class, () -> spaceService.allocate(1, alloc));
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("Free: Should decrease used capacity but never go below 0")
    void testFreeCapacity() {
        SpaceAllocationDTO freeDto = new SpaceAllocationDTO();
        freeDto.setAmount(50); // 40 - 50 = -10 (Should reset to 0)

        when(repo.findById(1)).thenReturn(Optional.of(mockSpace));
        when(repo.save(any(Space.class))).thenReturn(mockSpace);

        Space result = spaceService.free(1, freeDto);

        assertEquals(0, result.getUsedCapacity());
        assertEquals(100, result.getAvailableCapacity());
    }

    @Test
    @DisplayName("Delete: Should throw error if zone still contains items")
    void testDeleteNonEmptyZone() {
        when(repo.findById(1)).thenReturn(Optional.of(mockSpace)); // UsedCapacity is 40

        BusinessException ex = assertThrows(BusinessException.class, () -> spaceService.delete(1));
        assertTrue(ex.getMessage().contains("still contains"));
        verify(repo, never()).delete(any());
    }

    @Test
    @DisplayName("Update: Should throw error if new capacity is less than current usage")
    void testUpdateInvalidCapacity() {
        SpaceDTO updateDto = new SpaceDTO();
        updateDto.setZone("Zone A");
        updateDto.setTotalCapacity(30); // Current used is 40

        when(repo.findById(1)).thenReturn(Optional.of(mockSpace));

        assertThrows(BusinessException.class, () -> spaceService.update(1, updateDto));
    }
}