package com.temple.donation.service;

import com.temple.donation.dto.DevoteeSummaryDto;
import com.temple.donation.dto.EntryDto;
import com.temple.donation.dto.MonthlyReportDto;
import com.temple.donation.dto.OccasionAmountDto;
import com.temple.donation.dto.OccasionSummaryDto;
import com.temple.donation.entity.Donation;
import com.temple.donation.repository.DonationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final DonationRepository donationRepository;

    public ReportService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    @Transactional(readOnly = true)
    public MonthlyReportDto buildReport(int year, List<Integer> months, List<String> devotees) {
        List<Integer> monthFilter = months == null ? List.of()
                : months.stream().filter(m -> m != null && m >= 1 && m <= 12).distinct().toList();
        Set<String> devoteeFilter = devotees == null ? Set.of()
                : devotees.stream()
                        .filter(d -> d != null)
                        .map(d -> d.trim().toLowerCase())
                        .filter(d -> !d.isEmpty())
                        .collect(Collectors.toUnmodifiableSet());

        List<EntryDto> entries = new ArrayList<>();
        Map<String, OccasionAccum> occasions = new LinkedHashMap<>();
        Map<String, DevoteeAccum> devoteesByMap = new LinkedHashMap<>();

        for (Donation donation : donationRepository.findAllWithSheet()) {
            if (donation.getDate() == null || donation.getDate().getYear() != year) {
                continue;
            }
            int month = donation.getDate().getMonthValue();
            if (!monthFilter.isEmpty() && !monthFilter.contains(month)) {
                continue;
            }
            String rawName = donation.getDevoteeName() == null ? "" : donation.getDevoteeName();
            String nameKey = rawName.trim().toLowerCase();
            if (!devoteeFilter.isEmpty() && !devoteeFilter.contains(nameKey)) {
                continue;
            }

            BigDecimal amount = donation.getAmount() == null ? BigDecimal.ZERO : donation.getAmount();
            String occasion = donation.getOccasion() == null || donation.getOccasion().isBlank()
                    ? "Other"
                    : donation.getOccasion().trim();

            entries.add(new EntryDto(
                    donation.getId(),
                    rawName.trim(),
                    donation.getOccasion(),
                    amount,
                    donation.getDate(),
                    donation.getMobile(),
                    donation.getNotes(),
                    donation.getSheet() == null ? "" : donation.getSheet().getName()));

            occasions.computeIfAbsent(occasion, k -> new OccasionAccum())
                    .add(amount);

            DevoteeAccum devAccum = devoteesByMap.computeIfAbsent(nameKey, k -> new DevoteeAccum(rawName.trim()));
            devAccum.add(amount, occasion);
        }

        entries.sort(Comparator.comparing(EntryDto::date));

        List<OccasionSummaryDto> byOccasion = occasions.entrySet().stream()
                .map(e -> new OccasionSummaryDto(e.getKey(), e.getValue().total, e.getValue().count))
                .sorted(Comparator.comparing(OccasionSummaryDto::total).reversed())
                .toList();

        List<DevoteeSummaryDto> byDevotee = devoteesByMap.values().stream()
                .map(a -> new DevoteeSummaryDto(
                        a.displayName,
                        a.total,
                        a.count,
                        a.occasions.entrySet().stream()
                                .map(e -> new OccasionAmountDto(e.getKey(), e.getValue()))
                                .sorted(Comparator.comparing(OccasionAmountDto::amount).reversed())
                                .toList()))
                .sorted(Comparator.comparing(DevoteeSummaryDto::total).reversed())
                .toList();

        BigDecimal total = entries.stream()
                .map(EntryDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyReportDto(year, entries, total, entries.size(), byOccasion, byDevotee);
    }

    @Transactional(readOnly = true)
    public List<String> devoteesInScope(int year, List<Integer> months) {
        List<Integer> monthFilter = months == null ? List.of()
                : months.stream().filter(m -> m != null && m >= 1 && m <= 12).distinct().toList();
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Donation donation : donationRepository.findAllWithSheet()) {
            if (donation.getDate() == null || donation.getDate().getYear() != year) {
                continue;
            }
            if (!monthFilter.isEmpty() && !monthFilter.contains(donation.getDate().getMonthValue())) {
                continue;
            }
            if (donation.getDevoteeName() != null && !donation.getDevoteeName().isBlank()) {
                names.add(donation.getDevoteeName().trim());
            }
        }
        return new ArrayList<>(names);
    }

    private static final class OccasionAccum {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        void add(BigDecimal amount) {
            total = total.add(amount);
            count++;
        }
    }

    private static final class DevoteeAccum {
        final String displayName;
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        final Map<String, BigDecimal> occasions = new LinkedHashMap<>();

        DevoteeAccum(String displayName) {
            this.displayName = displayName;
        }

        void add(BigDecimal amount, String occasion) {
            total = total.add(amount);
            count++;
            occasions.merge(occasion, amount, BigDecimal::add);
        }
    }
}
