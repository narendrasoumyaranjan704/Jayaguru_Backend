package com.temple.donation.dto;

import java.math.BigDecimal;

public record OccasionSummaryDto(String occasion, BigDecimal total, int count) {
}
