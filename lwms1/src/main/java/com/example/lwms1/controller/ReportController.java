package com.example.lwms1.controller;

import com.example.lwms1.dto.ReportDTO;
import com.example.lwms1.model.Report;
import com.example.lwms1.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/reports")
// 1. Temporarily comment out Security to see if the page loads
// @PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    @GetMapping
    public String list(Model model) {
        // 2. Ensure the attribute name "reports" matches your th:each="r : ${reports}"
        List<Report> allReports = service.listAll();
        model.addAttribute("reports", allReports);

        // 3. This MUST be here or th:object="${form}" will cause a 500 error
        model.addAttribute("form", new ReportDTO());

        // 4. Ensure this path is EXACTLY where your file is
        return "report/list";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute("form") ReportDTO dto) {
        service.generate(dto);
        return "redirect:/admin/reports";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/admin/reports";
    }
}