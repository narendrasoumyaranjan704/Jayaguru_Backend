package com.temple.donation.dto;

import java.util.List;

public record PagedDonationsDto(List<?> items, long total, int page, int size, int totalPages) {
}
