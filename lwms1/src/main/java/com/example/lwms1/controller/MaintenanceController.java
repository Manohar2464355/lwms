package com.example.lwms1.controller;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.model.Space;
import com.example.lwms1.service.MaintenanceService;
import com.example.lwms1.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/maintenance")
@PreAuthorize("hasRole('ADMIN')")
public class MaintenanceController {

    private final MaintenanceService service;
    private final SpaceService spaceService;

    public MaintenanceController(MaintenanceService service, SpaceService spaceService) {
        this.service = service;
        this.spaceService = spaceService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("schedules", service.listAll());
        model.addAttribute("availableSpaces", spaceService.listAll());
        model.addAttribute("form", new MaintenanceDTO());
        return "maintenance/schedule";
    }

    @PostMapping("/schedule")
    public String schedule(@ModelAttribute("form") @Valid MaintenanceDTO dto,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            System.out.println("Errors: " + result.getAllErrors());
            model.addAttribute("schedules", service.listAll());
            model.addAttribute("availableSpaces", spaceService.listAll());
            return "maintenance/schedule";
        }
        service.schedule(dto);
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("form") @Valid MaintenanceDTO dto,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("schedules", service.listAll());
            model.addAttribute("availableSpaces", spaceService.listAll());
            return "maintenance/schedule";
        }
        service.update(id, dto);
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/admin/maintenance";
    }
}