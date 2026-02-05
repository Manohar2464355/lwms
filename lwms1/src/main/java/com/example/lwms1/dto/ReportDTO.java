package com.example.lwms1.dto;

import jakarta.validation.constraints.NotBlank;

public class ReportDTO {
    @NotBlank(message = "Please select a report type")
    private String reportType;
    private String customNotes;

    public ReportDTO() {}
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getCustomNotes() { return customNotes; }
    public void setCustomNotes(String customNotes) { this.customNotes = customNotes; }
}