package com.example.lwms1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    private String reportType; // e.g., "INVENTORY", "SHIPMENT", "SPACE", "MAINTENANCE"
    private LocalDateTime generatedOn;

    @Column(columnDefinition = "TEXT")
    private String details;

    public Report() {}

    public String getFormattedDate() {
        return generatedOn != null ? generatedOn.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    // Getters and Setters
    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public LocalDateTime getGeneratedOn() { return generatedOn; }
    public void setGeneratedOn(LocalDateTime generatedOn) { this.generatedOn = generatedOn; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}