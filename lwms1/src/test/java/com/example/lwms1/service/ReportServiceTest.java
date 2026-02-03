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
    @DisplayName("Generate: Inventory Report should correctly format item data")
    void testGenerateInventoryReport() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("INVENTORY");
        dto.setCustomNotes("Stock audit");

        Inventory item = new Inventory();
        item.setItemName("Smartphone");
        item.setQuantity(100);
        item.setLocation("Warehouse-B");

        when(inventoryRepo.findAll()).thenReturn(Collections.singletonList(item));

        // Act
        reportService.generate(dto);

        // Assert
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepo).save(captor.capture());

        Report result = captor.getValue();
        String details = result.getDetails();

        assertEquals("INVENTORY", result.getReportType());
        assertTrue(details.contains("Smartphone"), "Report should contain item name");
        assertTrue(details.contains("100"), "Report should contain quantity");
        assertTrue(details.contains("Stock audit"), "Report should contain custom notes");
        verify(inventoryRepo).findAll();
    }

    @Test
    @DisplayName("Generate: Shipment Report should include shipment status and destination")
    void testGenerateShipmentReport() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("SHIPMENT");

        Shipment shipment = new Shipment();
        shipment.setShipmentId(501);
        shipment.setDestination("New York");
        shipment.setStatus("SHIPPED");

        when(shipmentRepo.findAll()).thenReturn(Collections.singletonList(shipment));

        // Act
        reportService.generate(dto);

        // Assert
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepo).save(captor.capture());

        String details = captor.getValue().getDetails();
        assertTrue(details.contains("#SH-501"), "Should match the #SH- format in service");
        assertTrue(details.contains("New York"));
        assertTrue(details.contains("SHIPPED"));
    }

    @Test
    @DisplayName("Generate: Space Report should include zone and capacity details")
    void testGenerateSpaceReport() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("SPACE");

        Space space = new Space();
        space.setZone("Loading-Dock");
        space.setTotalCapacity(500);
        space.setUsedCapacity(150);

        when(spaceRepo.findAll()).thenReturn(Collections.singletonList(space));

        // Act
        reportService.generate(dto);

        // Assert
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepo).save(captor.capture());

        String details = captor.getValue().getDetails();
        assertTrue(details.contains("Loading-Dock"));
        assertTrue(details.contains("500"));
        assertTrue(details.contains("150"));
    }

    @Test
    @DisplayName("Generate: Maintenance Report should include task description and status")
    void testGenerateMaintenanceReport() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("MAINTENANCE");

        MaintenanceSchedule task = new MaintenanceSchedule();
        task.setEquipmentId(202);
        task.setDescription("HVAC Repair");
        task.setCompletionStatus("IN_PROGRESS");
        task.setScheduledDate(LocalDate.now());

        when(maintenanceRepo.findAll()).thenReturn(Collections.singletonList(task));

        // Act
        reportService.generate(dto);

        // Assert
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepo).save(captor.capture());

        String details = captor.getValue().getDetails();
        assertTrue(details.contains("HVAC Repair"));
        assertTrue(details.contains("IN_PROGRESS"));
        assertTrue(details.contains("202"));
    }
}