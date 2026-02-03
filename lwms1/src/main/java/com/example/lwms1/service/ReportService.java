package com.example.lwms1.service;

import com.example.lwms1.dto.ReportDTO;
import com.example.lwms1.model.*;
import com.example.lwms1.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepo;
    private final InventoryRepository inventoryRepo;
    private final ShipmentRepository shipmentRepo;
    private final SpaceRepository spaceRepo;
    private final MaintenanceScheduleRepository maintenanceRepo;

    @Autowired
    public ReportService(ReportRepository reportRepo, InventoryRepository inventoryRepo,
                         ShipmentRepository shipmentRepo, SpaceRepository spaceRepo,
                         MaintenanceScheduleRepository maintenanceRepo) {
        this.reportRepo = reportRepo;
        this.inventoryRepo = inventoryRepo;
        this.shipmentRepo = shipmentRepo;
        this.spaceRepo = spaceRepo;
        this.maintenanceRepo = maintenanceRepo;
    }

    public List<Report> listAll() {
        return reportRepo.findAll();
    }

    public void generate(ReportDTO dto) {
        Report report = new Report();
        report.setReportType(dto.getReportType());
        report.setGeneratedOn(LocalDateTime.now());

        StringBuilder content = new StringBuilder();
        content.append("===== ").append(dto.getReportType()).append(" REPORT =====\n");
        content.append("Date: ").append(LocalDateTime.now()).append("\n\n");

        String type = dto.getReportType();

        // SIMPLIFIED LOGIC USING FOR-LOOPS INSTEAD OF STREAMS/FOREACH
        if ("INVENTORY".equals(type)) {
            content.append(String.format("%-20s | %-10s | %-15s\n", "Item", "Qty", "Location"));
            content.append("----------------------------------------------------------\n");
            List<Inventory> items = inventoryRepo.findAll();
            for (Inventory item : items) {
                content.append(String.format("%-20s | %-10d | %-15s\n",
                        item.getItemName(), item.getQuantity(), item.getLocation()));
            }
        }
        else if ("SHIPMENT".equals(type)) {
            content.append(String.format("%-10s | %-20s | %-12s\n", "ID", "Dest", "Status"));
            content.append("----------------------------------------------------------\n");
            List<Shipment> shipments = shipmentRepo.findAll();
            for (Shipment s : shipments) {
                content.append(String.format("#SH-%-6d | %-20s | %-12s\n",
                        s.getShipmentId(), s.getDestination(), s.getStatus()));
            }
        }
        else if ("SPACE".equals(type)) {
            content.append(String.format("%-15s | %-10s | %-10s\n", "Zone", "Total", "Used"));
            content.append("----------------------------------------------------------\n");
            List<Space> spaces = spaceRepo.findAll();
            for (Space space : spaces) {
                content.append(String.format("%-15s | %-10d | %-10d\n",
                        space.getZone(), space.getTotalCapacity(), space.getUsedCapacity()));
            }
        }
        else if ("MAINTENANCE".equals(type)) {
            content.append(String.format("%-12s | %-20s | %-10s\n", "ID", "Desc", "Status"));
            content.append("----------------------------------------------------------\n");
            List<MaintenanceSchedule> tasks = maintenanceRepo.findAll();
            for (MaintenanceSchedule m : tasks) {
                content.append(String.format("%-12d | %-20s | %-10s\n",
                        m.getEquipmentId(), m.getDescription(), m.getCompletionStatus()));
            }
        }

        // Add admin notes if they exist
        if (dto.getCustomNotes() != null && !dto.getCustomNotes().isEmpty()) {
            content.append("\nNOTES: ").append(dto.getCustomNotes());
        }

        report.setDetails(content.toString());
        reportRepo.save(report);
    }

    public void delete(Integer id) {
        reportRepo.deleteById(id);
    }
}