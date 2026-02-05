package com.example.lwms1.controller;

import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.model.Inventory;
// Import your MaintenanceService
import com.example.lwms1.service.MaintenanceService;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final SpaceService spaceService;
    private final MaintenanceService maintenanceService;

    @Autowired
    public InventoryController(InventoryService invService, SpaceService spaceService, MaintenanceService maintService) {
        this.inventoryService = invService;
        this.spaceService = spaceService;
        this.maintenanceService = maintService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", inventoryService.listAll());
        model.addAttribute("inventoryDTO", new InventoryDTO());
        populateSpaceData(model);
        return "admin/inventory/list";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        InventoryDTO existingItem = inventoryService.getDtoById(id);
        model.addAttribute("inventoryDTO", existingItem);
        populateSpaceData(model);
        return "admin/inventory/edit";
    }
    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute("inventoryDTO") InventoryDTO dto,
                          BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("items", inventoryService.listAll());
            populateSpaceData(model);
            return "admin/inventory/list";
        }
        inventoryService.create(dto);
        ra.addFlashAttribute("successMessage", "Item added successfully!");
        return "redirect:/inventory";
    }

    @PostMapping("/update/{id}")
    public String updateItem(@PathVariable Integer id,
                             @Valid @ModelAttribute("inventoryDTO") InventoryDTO dto,
                             BindingResult result, RedirectAttributes ra, Model model) {

        System.out.println("Update triggered for ID: " + id); // DEBUG LINE 1

        if (result.hasErrors()) {
            System.out.println("Validation errors found: " + result.getAllErrors()); // DEBUG LINE 2
            populateSpaceData(model);
            return "admin/inventory/edit";
        }

        inventoryService.update(id, dto);
        ra.addFlashAttribute("successMessage", "Item updated successfully!");
        return "redirect:/inventory";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        inventoryService.delete(id);
        ra.addFlashAttribute("successMessage", "Item removed.");
        return "redirect:/inventory";
    }

    private void populateSpaceData(Model model) {
        model.addAttribute("spaces", spaceService.listAll());
        model.addAttribute("lockedSpaceIds", maintenanceService.getCurrentlyLockedSpaceIds());
    }
}