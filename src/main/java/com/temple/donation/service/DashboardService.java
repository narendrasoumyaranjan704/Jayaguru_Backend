package com.temple.donation.service;

import com.temple.donation.dto.DashboardDto;
import com.temple.donation.dto.EntryDto;
import com.temple.donation.dto.OccasionTotalDto;
import com.temple.donation.entity.Donation;
import com.temple.donation.entity.DonationSheet;
import com.temple.donation.repository.DonationSheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {

    private final DonationSheetRepository sheetRepository;

    public DashboardService(DonationSheetRepository sheetRepository) {
        this.sheetRepository = sheetRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        List<DonationSheet> sheets = sheetRepository.findAllWithDonations();
        int totalSheets = sheets.size();

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, BigDecimal> occasionTotals = new LinkedHashMap<>();
        List<EntryDto> recent = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (DonationSheet sheet : sheets) {
            for (Donation donation : sheet.getDonations()) {
                BigDecimal amount = donation.getAmount() == null ? BigDecimal.ZERO : donation.getAmount();
                totalAmount = totalAmount.add(amount);
                String occasion = donation.getOccasion() == null || donation.getOccasion().isBlank()
                        ? "Other"
                        : donation.getOccasion().trim();
                occasionTotals.merge(occasion, amount, BigDecimal::add);
                recent.add(new EntryDto(
                        donation.getId(),
                        donation.getDevoteeName() == null ? "" : donation.getDevoteeName().trim(),
                        donation.getOccasion(),
                        amount,
                        donation.getDate(),
                        donation.getMobile(),
                        donation.getNotes(),
                        sheet.getName()));
            }
        }

        long totalDonations = recent.size();
        BigDecimal currentMonthAmount = recent.stream()
                .filter(e -> e.date() != null
                        && e.date().getYear() == now.getYear()
                        && e.date().getMonthValue() == now.getMonthValue())
                .map(EntryDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        recent.sort(Comparator.comparing(EntryDto::date, Comparator.nullsLast(Comparator.reverseOrder())));
        List<EntryDto> recentDonations = recent.stream().limit(8).toList();

        List<OccasionTotalDto> topOccasions = occasionTotals.entrySet().stream()
                .map(e -> new OccasionTotalDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(OccasionTotalDto::total).reversed())
                .limit(5)
                .toList();

        String currentMonth = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + now.getYear();

        return new DashboardDto(
                totalSheets,
                totalDonations,
                totalAmount,
                currentMonth,
                currentMonthAmount,
                recentDonations,
                topOccasions);
    }
}
