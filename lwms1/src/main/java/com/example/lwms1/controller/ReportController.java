package com.example.lwms1.controller;

import com.example.lwms1.dto.ReportDTO;
import com.example.lwms1.model.Report;
import com.example.lwms1.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService service;

    @Autowired
    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        List<Report> allReports = service.listAll();
        model.addAttribute("reports", allReports);
        model.addAttribute("form", new ReportDTO());
        return "admin/report/list";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute("form") ReportDTO dto, RedirectAttributes ra) {
        service.generate(dto);
        ra.addFlashAttribute("successMessage", "Report for " + dto.getReportType() + " generated!");
        return "redirect:/admin/reports";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("successMessage", "Report deleted successfully.");
        return "redirect:/admin/reports";
    }
}