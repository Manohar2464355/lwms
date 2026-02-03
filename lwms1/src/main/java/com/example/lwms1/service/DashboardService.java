package com.example.lwms1.service;

import com.example.lwms1.model.Space;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final InventoryService inventoryService;
    private final ShipmentService shipmentService;
    private final MaintenanceService maintenanceService;
    private final ReportService reportService;
    private final SpaceService spaceService;
    private final UserService userService; // Only for Admin dashboard

    @Autowired
    public DashboardService(InventoryService inventoryService, ShipmentService shipmentService,
                            MaintenanceService maintenanceService, ReportService reportService,
                            SpaceService spaceService, UserService userService) {
        this.inventoryService = inventoryService;
        this.shipmentService = shipmentService;
        this.maintenanceService = maintenanceService;
        this.reportService = reportService;
        this.spaceService = spaceService;
        this.userService = userService;
    }

    public Map<String, Object> getAllStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Basic Counts
        stats.put("inventoryCount", inventoryService.listAll().size());
        stats.put("activeShipmentsCount", shipmentService.listAll().size());
        stats.put("openMaintenanceTasks", maintenanceService.getPendingMaintenanceCount());
        stats.put("reportCount", reportService.listAll().size());
        stats.put("userCount", userService.listAll().size());

        // 2. The "Business Logic" Math
        List<Space> spaces = spaceService.listAll();
        double totalUsed = spaces.stream().mapToDouble(s -> s.getUsedCapacity() != null ? s.getUsedCapacity() : 0).sum();
        double totalMax = spaces.stream().mapToDouble(s -> s.getTotalCapacity() != null ? s.getTotalCapacity() : 0).sum();
        double utilization = (totalMax > 0) ? (totalUsed / totalMax) * 100 : 0;

        stats.put("warehouseUtilization", String.format("%.1f%%", utilization));

        return stats;
    }
}