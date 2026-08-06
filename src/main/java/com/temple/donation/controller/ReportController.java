package com.temple.donation.controller;

import com.temple.donation.dto.MonthlyReportDto;
import com.temple.donation.service.ExcelExportService;
import com.temple.donation.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ExcelExportService excelExportService;

    public ReportController(ReportService reportService, ExcelExportService excelExportService) {
        this.reportService = reportService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/monthly")
    public MonthlyReportDto monthly(
            @RequestParam int year,
            @RequestParam(required = false) List<Integer> months,
            @RequestParam(required = false) List<String> devotees) {
        return reportService.buildReport(year, months, devotees);
    }

    @GetMapping("/monthly/export")
    public ResponseEntity<byte[]> exportMonthly(
            @RequestParam int year,
            @RequestParam(required = false) List<Integer> months,
            @RequestParam(required = false) List<String> devotees) {
        MonthlyReportDto report = reportService.buildReport(year, months, devotees);
        byte[] bytes = excelExportService.monthlyReportExcel(report, year, months, devotees);
        String fileName = excelExportService.fileName(year, months, devotees);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/devotees")
    public List<String> devotees(
            @RequestParam int year,
            @RequestParam(required = false) List<Integer> months) {
        return reportService.devoteesInScope(year, months);
    }
}
