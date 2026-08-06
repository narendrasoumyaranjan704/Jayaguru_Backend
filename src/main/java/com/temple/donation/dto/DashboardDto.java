package com.temple.donation.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        int totalSheets,
        long totalDonations,
        BigDecimal totalAmount,
        String currentMonth,
        BigDecimal currentMonthAmount,
        List<EntryDto> recentDonations,
        List<OccasionTotalDto> topOccasions) {
}
