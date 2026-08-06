package com.temple.donation.service;

import com.temple.donation.dto.EntryDto;
import com.temple.donation.dto.MonthlyReportDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public byte[] monthlyReportExcel(MonthlyReportDto report, int year, List<Integer> months, List<String> devotees) {
        String monthsLabel = monthsLabel(months);
        String devoteesLabel = devoteesLabel(devotees);
        String scope = year + " — " + monthsLabel
                + (devotees == null || devotees.isEmpty() ? "" : " — " + devoteesLabel);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet donationsSheet = workbook.createSheet("Donations");
            writeDonationsSheet(donationsSheet, report, scope);

            Sheet devoteeSheet = workbook.createSheet("Devotee Summary");
            writeDevoteeSheet(devoteeSheet, report);

            Sheet occasionSheet = workbook.createSheet("Occasion Summary");
            writeOccasionSheet(occasionSheet, report);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    public String fileName(int year, List<Integer> months, List<String> devotees) {
        String monthPart;
        if (months == null || months.isEmpty()) {
            monthPart = "all_months";
        } else if (months.size() == 1) {
            monthPart = MONTHS[months.get(0) - 1];
        } else if (months.size() == 12) {
            monthPart = "all_months";
        } else {
            monthPart = months.size() + "_months";
        }
        String devoteePart = "";
        if (devotees != null && devotees.size() == 1 && devotees.get(0) != null && !devotees.get(0).isBlank()) {
            devoteePart = "_" + devotees.get(0).trim().replaceAll("\\s+", "_");
        }
        return "donations_" + year + "_" + monthPart + devoteePart + ".xlsx";
    }

    private void writeDonationsSheet(Sheet sheet, MonthlyReportDto report, String scope) {
        sheet.setColumnWidth(0, 14 * 256);
        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(2, 30 * 256);
        sheet.setColumnWidth(3, 26 * 256);
        sheet.setColumnWidth(4, 14 * 256);

        int r = 0;
        Row title = sheet.createRow(r++);
        title.createCell(0).setCellValue("Temple Donation Report");
        Row subtitle = sheet.createRow(r++);
        subtitle.createCell(0).setCellValue("Temple Donation — " + scope);
        Row scopeRow = sheet.createRow(r++);
        scopeRow.createCell(0).setCellValue("Scope: " + scope);
        r++;

        String[] headers = {"Date", "Devotee", "Occasion", "Sheet", "Amount (\u20B9)"};
        Row headerRow = sheet.createRow(r++);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (EntryDto entry : report.entries()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(entry.date() == null ? "" : entry.date().toString());
            row.createCell(1).setCellValue(nullToEmpty(entry.devoteeName()));
            row.createCell(2).setCellValue(nullToEmpty(entry.occasion()));
            row.createCell(3).setCellValue(nullToEmpty(entry.sheetName()));
            row.createCell(4).setCellValue(entry.amount() == null ? 0 : entry.amount().doubleValue());
        }

        r++;
        Row totalRow = sheet.createRow(r);
        totalRow.createCell(0).setCellValue("Total");
        totalRow.createCell(4).setCellValue(report.total() == null ? 0 : report.total().doubleValue());
    }

    private void writeDevoteeSheet(Sheet sheet, MonthlyReportDto report) {
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(1, 12 * 256);
        sheet.setColumnWidth(2, 14 * 256);

        int r = 0;
        Row header = sheet.createRow(r++);
        header.createCell(0).setCellValue("Devotee");
        header.createCell(1).setCellValue("Donations");
        header.createCell(2).setCellValue("Total (\u20B9)");

        int[] rowIndex = {r};
        report.byDevotee().forEach(d -> {
            Row row = sheet.createRow(rowIndex[0]++);
            row.createCell(0).setCellValue(d.devoteeName());
            row.createCell(1).setCellValue(d.count());
            row.createCell(2).setCellValue(d.total() == null ? 0 : d.total().doubleValue());
        });

        int totalRowIndex = rowIndex[0] + 1;
        Row totalRow = sheet.createRow(totalRowIndex);
        totalRow.createCell(0).setCellValue("Total");
        totalRow.createCell(2).setCellValue(report.total() == null ? 0 : report.total().doubleValue());
    }

    private void writeOccasionSheet(Sheet sheet, MonthlyReportDto report) {
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(1, 12 * 256);
        sheet.setColumnWidth(2, 14 * 256);

        int r = 0;
        Row header = sheet.createRow(r++);
        header.createCell(0).setCellValue("Occasion");
        header.createCell(1).setCellValue("Count");
        header.createCell(2).setCellValue("Total (\u20B9)");

        int[] rowIndex = {1};
        report.byOccasion().forEach(o -> {
            Row row = sheet.createRow(rowIndex[0]++);
            row.createCell(0).setCellValue(o.occasion());
            row.createCell(1).setCellValue(o.count());
            row.createCell(2).setCellValue(o.total() == null ? 0 : o.total().doubleValue());
        });
    }

    private String monthsLabel(List<Integer> months) {
        if (months == null || months.isEmpty()) {
            return "No months";
        }
        if (months.size() == 12) {
            return "All months";
        }
        if (months.size() == 1 && months.get(0) != null) {
            return MONTHS[months.get(0) - 1];
        }
        return months.size() + " months";
    }

    private String devoteesLabel(List<String> devotees) {
        if (devotees == null || devotees.isEmpty()) {
            return "All devotees";
        }
        List<String> trimmed = devotees.stream()
                .filter(d -> d != null && !d.isBlank())
                .map(String::trim)
                .toList();
        if (trimmed.size() <= 3) {
            return String.join(", ", trimmed);
        }
        return trimmed.size() + " devotees";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
