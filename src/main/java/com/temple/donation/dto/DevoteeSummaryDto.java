package com.temple.donation.dto;

import java.math.BigDecimal;
import java.util.List;

public record DevoteeSummaryDto(
        String devoteeName,
        BigDecimal total,
        int count,
        List<OccasionAmountDto> occasions) {
}
