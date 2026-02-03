package com.example.lwms1.service;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MaintenanceServiceTest {

    @Mock
    private MaintenanceScheduleRepository repo;

    @InjectMocks
    private MaintenanceService service;

    private MaintenanceSchedule mockTask;

    @BeforeEach
    void setUp() {
        mockTask = new MaintenanceSchedule();
        // Matching your model's field names
        mockTask.setScheduleId(1);
        mockTask.setEquipmentId(101);
        mockTask.setCompletionStatus("PENDING");
        mockTask.setDescription("Standard Maintenance");
        // Using LocalDate to match your model exactly
        mockTask.setScheduledDate(LocalDate.now());
    }

    @Test
    @DisplayName("Toggle Status: Should switch from PENDING to COMPLETED")
    void testToggleStatusToCompleted() {
        // Arrange
        when(repo.findById(1)).thenReturn(Optional.of(mockTask));
        when(repo.save(any(MaintenanceSchedule.class))).thenReturn(mockTask);

        // Act
        String resultStatus = service.toggleStatus(1);

        // Assert
        assertEquals("COMPLETED", resultStatus);
        assertEquals("COMPLETED", mockTask.getCompletionStatus());
        verify(repo).save(mockTask);
    }

    @Test
    @DisplayName("Toggle Status: Should switch from COMPLETED back to PENDING")
    void testToggleStatusToPending() {
        // Arrange
        mockTask.setCompletionStatus("COMPLETED");
        when(repo.findById(1)).thenReturn(Optional.of(mockTask));
        when(repo.save(any(MaintenanceSchedule.class))).thenReturn(mockTask);

        // Act
        String resultStatus = service.toggleStatus(1);

        // Assert
        assertEquals("PENDING", resultStatus);
        verify(repo).save(mockTask);
    }

    @Test
    @DisplayName("Schedule: Should default to PENDING status when no status is provided in DTO")
    void testScheduleDefaultsToPending() {
        // Arrange
        MaintenanceDTO dto = new MaintenanceDTO();
        dto.setEquipmentId(101);
        dto.setDescription("New Floor Repair");
        dto.setScheduledDate(LocalDate.now());

        when(repo.save(any(MaintenanceSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MaintenanceSchedule saved = service.schedule(dto);

        // Assert
        assertEquals("PENDING", saved.getCompletionStatus());
        verify(repo).save(any(MaintenanceSchedule.class));
    }

    @Test
    @DisplayName("Get Count: Should return the number of pending tasks")
    void testGetPendingMaintenanceCount() {
        // Arrange
        when(repo.countByCompletionStatusIgnoreCase("PENDING")).thenReturn(10L);

        // Act
        long count = service.getPendingMaintenanceCount();

        // Assert
        assertEquals(10L, count);
        verify(repo).countByCompletionStatusIgnoreCase("PENDING");
    }

    @Test
    @DisplayName("Error Handling: Should throw ResourceNotFoundException for invalid ID")
    void testToggleWithInvalidIdThrowsException() {
        // Arrange
        when(repo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.toggleStatus(999));
        verify(repo, never()).save(any());
    }
}