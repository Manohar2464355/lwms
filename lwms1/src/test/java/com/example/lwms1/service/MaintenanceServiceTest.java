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

import java.time.LocalDate; // Use LocalDate, not LocalDateTime
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

        // FIX 1: Match scheduleId from your model
        mockTask.setScheduleId(1);

        mockTask.setEquipmentId(101);
        mockTask.setCompletionStatus("PENDING");
        mockTask.setDescription("Floor Repair");

        // FIX 2: Use LocalDate.now() to match your model's LocalDate type
        mockTask.setScheduledDate(LocalDate.now());
    }

    @Test
    @DisplayName("Toggle: Should change PENDING to COMPLETED")
    void testToggleStatusToCompleted() {
        when(repo.findById(1)).thenReturn(Optional.of(mockTask));
        when(repo.save(any(MaintenanceSchedule.class))).thenReturn(mockTask);

        String newStatus = service.toggleStatus(1);

        assertEquals("COMPLETED", newStatus);
        assertEquals("COMPLETED", mockTask.getCompletionStatus());
        verify(repo).save(mockTask);
    }

    @Test
    @DisplayName("Toggle: Should change COMPLETED back to PENDING")
    void testToggleStatusToPending() {
        mockTask.setCompletionStatus("COMPLETED");
        when(repo.findById(1)).thenReturn(Optional.of(mockTask));
        // repo.save is usually called in toggleStatus, so we mock it to avoid issues
        when(repo.save(any(MaintenanceSchedule.class))).thenReturn(mockTask);

        String newStatus = service.toggleStatus(1);

        assertEquals("PENDING", newStatus);
        verify(repo).save(mockTask);
    }

    @Test
    @DisplayName("Schedule: Should default to PENDING status")
    void testScheduleDefaultsToPending() {
        MaintenanceDTO dto = new MaintenanceDTO();
        dto.setEquipmentId(101);
        dto.setDescription("New Task");

        // FIX 3: Ensure DTO also uses LocalDate
        dto.setScheduledDate(LocalDate.now());

        when(repo.save(any(MaintenanceSchedule.class))).thenAnswer(i -> i.getArgument(0));

        MaintenanceSchedule saved = service.schedule(dto);

        assertEquals("PENDING", saved.getCompletionStatus());
        verify(repo).save(any(MaintenanceSchedule.class));
    }

    @Test
    @DisplayName("Count: Should call optimized repository method")
    void testPendingCount() {
        when(repo.countByCompletionStatusIgnoreCase("PENDING")).thenReturn(5L);

        long count = service.getPendingMaintenanceCount();

        assertEquals(5L, count);
        verify(repo, times(1)).countByCompletionStatusIgnoreCase("PENDING");
    }

    @Test
    @DisplayName("Error: Should throw Exception if task ID is invalid")
    void testToggleWithInvalidId() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.toggleStatus(99));
        verify(repo, never()).save(any());
    }
}