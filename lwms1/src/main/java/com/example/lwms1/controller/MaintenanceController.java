package com.example.lwms1.controller;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.service.MaintenanceService;
import com.example.lwms1.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/maintenance")
public class MaintenanceController {

    private final MaintenanceService service;
    private final SpaceService spaceService;

    @Autowired
    public MaintenanceController(MaintenanceService service, SpaceService spaceService) {
        this.service = service;
        this.spaceService = spaceService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("schedules", service.listAll());
        model.addAttribute("availableSpaces", spaceService.listAll());
        model.addAttribute("form", new MaintenanceDTO());
        return "admin/maintenance/schedule";
    }

    @PostMapping("/schedule")
    public String schedule(@ModelAttribute("form") @Valid MaintenanceDTO dto,
                           BindingResult result, Model model, RedirectAttributes ra) {
        // If there are validation errors (like empty date), stay on the same page
        if (result.hasErrors()) {
            model.addAttribute("schedules", service.listAll());
            model.addAttribute("availableSpaces", spaceService.listAll());
            return "admin/maintenance/schedule";
        }

        service.schedule(dto);
        ra.addFlashAttribute("successMessage", "Maintenance scheduled! Zone is now locked.");
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes ra) {
        String successMsg = service.toggleStatusAndGetMessage(id);

        ra.addFlashAttribute("successMessage", successMsg);
        return "redirect:/admin/maintenance";
    }


    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("successMessage", "Record removed.");
        return "redirect:/admin/maintenance";
    }
}