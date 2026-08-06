package com.temple.donation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntryDto(
        String id,
        String devoteeName,
        String occasion,
        BigDecimal amount,
        LocalDate date,
        String mobile,
        String notes,
        String sheetName) {
}
