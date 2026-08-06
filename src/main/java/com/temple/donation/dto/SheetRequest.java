package com.temple.donation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SheetRequest(
        @NotBlank(message = "Sheet name is required") String name,
        @NotNull(message = "Year is required") Integer year,
        @NotNull(message = "Month is required") @Min(1) @Max(12) Integer month,
        String notes) {
}
