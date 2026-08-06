package com.temple.donation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DonationRequest(
        @NotBlank(message = "Devotee name is required") String devoteeName,
        @NotBlank(message = "Occasion is required") String occasion,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
        @NotNull(message = "Date is required") LocalDate date,
        String mobile,
        String notes) {
}
