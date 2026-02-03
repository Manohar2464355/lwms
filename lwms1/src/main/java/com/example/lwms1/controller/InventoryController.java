package com.example.lwms1.controller;

import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList; // Added for simplified logic
import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final SpaceService spaceService;
    private final MaintenanceScheduleRepository maintenanceRepo;

    @Autowired
    public InventoryController(InventoryService inventoryService,
                               SpaceService spaceService,
                               MaintenanceScheduleRepository maintenanceRepo) {
        this.inventoryService = inventoryService;
        this.spaceService = spaceService;
        this.maintenanceRepo = maintenanceRepo;
    }

    @GetMapping
    public String listInventory(Model model) {
        model.addAttribute("items", inventoryService.listAll());
        model.addAttribute("inventoryDTO", new InventoryDTO());
        model.addAttribute("spaces", spaceService.listAll());

        // --- SIMPLIFIED LOGIC (NO STREAMS) ---
        List<MaintenanceSchedule> allSchedules = maintenanceRepo.findAll();
        List<Integer> lockedSpaceIds = new ArrayList<>();

        for (MaintenanceSchedule schedule : allSchedules) {
            // Check if status is PENDING (meaning the zone is currently locked)
            if ("PENDING".equalsIgnoreCase(schedule.getCompletionStatus())) {
                lockedSpaceIds.add(schedule.getEquipmentId());
            }
        }

        model.addAttribute("lockedSpaceIds", lockedSpaceIds);
        return "admin/inventory/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute("inventoryDTO") InventoryDTO dto,
                          BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("items", inventoryService.listAll());
            model.addAttribute("spaces", spaceService.listAll());
            return "admin/inventory/list";
        }

        inventoryService.create(dto);
        ra.addFlashAttribute("successMessage", "Item added successfully!");
        return "redirect:/inventory";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{id}")
    public String updateItem(@PathVariable Integer id,
                             @Valid @ModelAttribute("inventoryDTO") InventoryDTO dto,
                             BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "admin/inventory/edit";
        }

        inventoryService.update(id, dto);
        ra.addFlashAttribute("successMessage", "Item updated successfully!");
        return "redirect:/inventory";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Integer id, RedirectAttributes ra) {
        inventoryService.delete(id);
        ra.addFlashAttribute("successMessage", "Item removed from inventory.");
        return "redirect:/inventory";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Inventory item = inventoryService.findById(id);

        InventoryDTO dto = new InventoryDTO();
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setCategory(item.getCategory());
        dto.setQuantity(item.getQuantity());
        dto.setLocation(item.getLocation());

        model.addAttribute("inventoryDTO", dto);
        model.addAttribute("spaces", spaceService.listAll());
        return "admin/inventory/edit";
    }
}