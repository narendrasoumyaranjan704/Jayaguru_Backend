package com.temple.donation.dto;

import java.math.BigDecimal;
import java.util.List;

public record SheetSummaryDto(BigDecimal total, int count, List<OccasionTotalDto> byOccasion) {
}
