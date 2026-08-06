package com.temple.donation.service;

import com.temple.donation.dto.DonationRequest;
import com.temple.donation.dto.OccasionTotalDto;
import com.temple.donation.dto.PagedDonationsDto;
import com.temple.donation.dto.SheetRequest;
import com.temple.donation.dto.SheetSummaryDto;
import com.temple.donation.entity.Donation;
import com.temple.donation.entity.DonationSheet;
import com.temple.donation.exception.NotFoundException;
import com.temple.donation.repository.DonationRepository;
import com.temple.donation.repository.DonationSheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SheetService {

    private final DonationSheetRepository sheetRepository;
    private final DonationRepository donationRepository;
    private final SeedService seedService;

    public SheetService(DonationSheetRepository sheetRepository,
                        DonationRepository donationRepository,
                        SeedService seedService) {
        this.sheetRepository = sheetRepository;
        this.donationRepository = donationRepository;
        this.seedService = seedService;
    }

    @Transactional(readOnly = true)
    public List<DonationSheet> getAll() {
        return sheetRepository.findAllWithDonations();
    }

    @Transactional(readOnly = true)
    public DonationSheet get(String id) {
        return sheetRepository.findByIdWithDonations(id)
                .orElseThrow(() -> new NotFoundException("Sheet not found: " + id));
    }

    @Transactional
    public DonationSheet create(SheetRequest request) {
        DonationSheet sheet = new DonationSheet();
        sheet.setName(request.name().trim());
        sheet.setYear(request.year());
        sheet.setMonth(request.month());
        sheet.setNotes(request.notes());
        return sheetRepository.save(sheet);
    }

    @Transactional
    public DonationSheet update(String id, SheetRequest request) {
        DonationSheet sheet = get(id);
        sheet.setName(request.name().trim());
        sheet.setYear(request.year());
        sheet.setMonth(request.month());
        sheet.setNotes(request.notes());
        return sheetRepository.save(sheet);
    }

    @Transactional
    public void delete(String id) {
        DonationSheet sheet = get(id);
        sheetRepository.delete(sheet);
    }

    @Transactional
    public Donation addDonation(String sheetId, DonationRequest request) {
        DonationSheet sheet = get(sheetId);
        Donation donation = new Donation();
        apply(donation, request);
        sheet.addDonation(donation);
        sheetRepository.save(sheet);
        return donation;
    }

    @Transactional
    public Donation updateDonation(String sheetId, String donationId, DonationRequest request) {
        DonationSheet sheet = get(sheetId);
        Donation donation = findDonation(sheet, donationId);
        apply(donation, request);
        sheetRepository.save(sheet);
        return donation;
    }

    @Transactional
    public void deleteDonation(String sheetId, String donationId) {
        DonationSheet sheet = get(sheetId);
        Donation donation = findDonation(sheet, donationId);
        sheet.removeDonation(donation);
        sheetRepository.save(sheet);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotal() {
        return donationRepository.sumAmount();
    }

    @Transactional(readOnly = true)
    public PagedDonationsDto getDonations(String sheetId, String search, int page, int size) {
        DonationSheet sheet = get(sheetId);
        String query = search == null ? "" : search.trim().toLowerCase();
        List<Donation> filtered = sheet.getDonations().stream()
                .filter(d -> query.isEmpty()
                        || (d.getDevoteeName() != null && d.getDevoteeName().toLowerCase().contains(query))
                        || (d.getOccasion() != null && d.getOccasion().toLowerCase().contains(query))
                        || (d.getMobile() != null && d.getMobile().contains(query)))
                .sorted(Comparator.comparing(Donation::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Donation::getId, Comparator.reverseOrder()))
                .toList();

        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) safeSize));
        int from = (safePage - 1) * safeSize;
        List<Donation> items = from >= filtered.size()
                ? List.of()
                : new ArrayList<>(filtered.subList(from, Math.min(from + safeSize, filtered.size())));
        return new PagedDonationsDto(items, filtered.size(), safePage, safeSize, totalPages);
    }

    @Transactional(readOnly = true)
    public SheetSummaryDto getSummary(String sheetId) {
        DonationSheet sheet = get(sheetId);
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> byOccasionMap = new LinkedHashMap<>();
        for (Donation donation : sheet.getDonations()) {
            BigDecimal amount = donation.getAmount() == null ? BigDecimal.ZERO : donation.getAmount();
            total = total.add(amount);
            String occasion = donation.getOccasion() == null || donation.getOccasion().isBlank()
                    ? "Other"
                    : donation.getOccasion().trim();
            byOccasionMap.merge(occasion, amount, BigDecimal::add);
        }
        List<OccasionTotalDto> byOccasion = byOccasionMap.entrySet().stream()
                .map(e -> new OccasionTotalDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(OccasionTotalDto::total).reversed())
                .toList();
        return new SheetSummaryDto(total, sheet.getDonations().size(), byOccasion);
    }

    @Transactional
    public List<DonationSheet> resetData() {
        sheetRepository.deleteAll();
        seedService.seedSheets();
        return getAll();
    }

    @Transactional
    public void clearData() {
        sheetRepository.deleteAll();
    }

    private void apply(Donation donation, DonationRequest request) {
        donation.setDevoteeName(request.devoteeName().trim());
        donation.setOccasion(request.occasion().trim());
        donation.setAmount(request.amount());
        donation.setDate(request.date());
        donation.setMobile(request.mobile());
        donation.setNotes(request.notes());
    }

    private Donation findDonation(DonationSheet sheet, String donationId) {
        return sheet.getDonations().stream()
                .filter(d -> d.getId().equals(donationId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Donation not found: " + donationId));
    }
}
