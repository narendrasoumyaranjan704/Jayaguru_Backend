package com.temple.donation.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReportDto(
        int year,
        List<EntryDto> entries,
        BigDecimal total,
        int count,
        List<OccasionSummaryDto> byOccasion,
        List<DevoteeSummaryDto> byDevotee) {
}
