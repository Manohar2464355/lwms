package com.example.lwms1.service;

import com.example.lwms1.dto.ReportDTO;
import com.example.lwms1.model.*;
import com.example.lwms1.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock private ReportRepository reportRepo;
    @Mock private InventoryRepository inventoryRepo;
    @Mock private ShipmentRepository shipmentRepo;
    @Mock private SpaceRepository spaceRepo;
    @Mock private MaintenanceScheduleRepository maintenanceRepo;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("Generate: Inventory Report should contain item details")
    void testGenerateInventoryReport() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("INVENTORY");
        dto.setCustomNotes("Monthly Check");

        Inventory item = new Inventory();
        item.setItemName("Gadget");
        item.setQuantity(50);
        item.setLocation("Zone-A");

        when(inventoryRepo.findAll()).thenReturn(Collections.singletonList(item));
        when(inventoryRepo.count()).thenReturn(1L);

        // Act
        reportService.generate(dto);

        // Assert
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepo).save(captor.capture());

        Report savedReport = captor.getValue();
        assertTrue(savedReport.getDetails().contains("Gadget"));
        assertTrue(savedReport.getDetails().contains("50"));
        assertTrue(savedReport.getDetails().contains("Monthly Check"));
        assertEquals("INVENTORY", savedReport.getReportType());
    }

    @Test
    @DisplayName("Generate: Space Report should format capacity correctly")
    void testGenerateSpaceReport() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("SPACE");

        Space space = new Space();
        space.setZone("Cold-Storage");
        space.setTotalCapacity(1000);
        space.setUsedCapacity(200);
        space.setAvailableCapacity(800);

        when(spaceRepo.findAll()).thenReturn(Collections.singletonList(space));

        // Act
        reportService.generate(dto);

        // Assert
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepo).save(captor.capture());

        String details = captor.getValue().getDetails();
        assertTrue(details.contains("Cold-Storage"));
        assertTrue(details.contains("1000"));
        assertTrue(details.contains("800"));
    }

    @Test
    @DisplayName("Generate: Maintenance Report should handle LocalDate correctly")
    void testGenerateMaintenanceReport() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("MAINTENANCE");

        MaintenanceSchedule m = new MaintenanceSchedule();
        m.setEquipmentId(101);
        m.setDescription("Repainting lines");
        m.setCompletionStatus("PENDING");
        m.setScheduledDate(LocalDate.now());

        when(maintenanceRepo.findAll()).thenReturn(Collections.singletonList(m));

        // Act
        reportService.generate(dto);

        // Assert
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepo).save(captor.capture());

        String details = captor.getValue().getDetails();
        assertTrue(details.contains("Repainting lines"));
        assertTrue(details.contains("PENDING"));
    }
}