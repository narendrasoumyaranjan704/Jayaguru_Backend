package com.temple.donation.controller;

import com.temple.donation.dto.DonationRequest;
import com.temple.donation.dto.MessageResponse;
import com.temple.donation.dto.PagedDonationsDto;
import com.temple.donation.dto.SheetRequest;
import com.temple.donation.dto.SheetSummaryDto;
import com.temple.donation.dto.TotalDto;
import com.temple.donation.entity.Donation;
import com.temple.donation.entity.DonationSheet;
import com.temple.donation.service.SheetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sheets")
public class SheetController {

    private final SheetService sheetService;

    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @GetMapping
    public List<DonationSheet> list() {
        return sheetService.getAll();
    }

    @GetMapping("/total")
    public TotalDto total() {
        return new TotalDto(sheetService.getTotal());
    }

    @GetMapping("/{id}")
    public DonationSheet get(@PathVariable String id) {
        return sheetService.get(id);
    }

    @PostMapping
    public DonationSheet create(@Valid @RequestBody SheetRequest request) {
        return sheetService.create(request);
    }

    @PutMapping("/{id}")
    public DonationSheet update(@PathVariable String id, @Valid @RequestBody SheetRequest request) {
        return sheetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable String id) {
        sheetService.delete(id);
        return MessageResponse.of("Sheet deleted");
    }

    @PostMapping("/{id}/donations")
    public Donation addDonation(@PathVariable String id, @Valid @RequestBody DonationRequest request) {
        return sheetService.addDonation(id, request);
    }

    @GetMapping("/{id}/donations")
    public PagedDonationsDto donations(@PathVariable String id,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return sheetService.getDonations(id, search, page, size);
    }

    @GetMapping("/{id}/summary")
    public SheetSummaryDto summary(@PathVariable String id) {
        return sheetService.getSummary(id);
    }

    @PutMapping("/{id}/donations/{donationId}")
    public Donation updateDonation(@PathVariable String id,
                                   @PathVariable String donationId,
                                   @Valid @RequestBody DonationRequest request) {
        return sheetService.updateDonation(id, donationId, request);
    }

    @DeleteMapping("/{id}/donations/{donationId}")
    public MessageResponse deleteDonation(@PathVariable String id, @PathVariable String donationId) {
        sheetService.deleteDonation(id, donationId);
        return MessageResponse.of("Donation deleted");
    }

    @PostMapping("/reset")
    public List<DonationSheet> reset() {
        return sheetService.resetData();
    }

    @DeleteMapping
    public MessageResponse clearAll() {
        sheetService.clearData();
        return MessageResponse.of("All data cleared");
    }
}
