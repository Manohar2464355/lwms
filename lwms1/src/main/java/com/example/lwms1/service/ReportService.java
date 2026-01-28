package com.example.lwms1.service;

import com.example.lwms1.dto.ReportDTO;
import com.example.lwms1.model.Report;
import com.example.lwms1.repository.ReportRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
    private final ReportRepository repo;
    public ReportService(ReportRepository repo) { this.repo = repo; }

    public List<Report> listAll() { return repo.findAll(); }

    public void generate(ReportDTO dto) {
        Report r = new Report();
        r.setReportType(dto.getReportType());
        r.setGeneratedOn(LocalDateTime.now());
        r.setDetails(dto.getDetails());
        repo.save(r);
    }

    public void delete(Integer id) { repo.deleteById(id); }
}