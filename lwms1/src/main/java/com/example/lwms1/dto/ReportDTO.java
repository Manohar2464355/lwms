package com.example.lwms1.dto;

import jakarta.validation.constraints.NotBlank;

public class ReportDTO {
    private Integer reportId;
    @NotBlank(message = "Required")
    private String reportType;
    private String details;

    public ReportDTO() {}
    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}