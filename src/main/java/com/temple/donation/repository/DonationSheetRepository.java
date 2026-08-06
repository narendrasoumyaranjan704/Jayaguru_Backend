package com.temple.donation.repository;

import com.temple.donation.entity.DonationSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationSheetRepository extends JpaRepository<DonationSheet, String> {

    @Query("select distinct s from DonationSheet s left join fetch s.donations order by s.year desc, s.month desc, s.createdAt asc")
    List<DonationSheet> findAllWithDonations();

    @Query("select distinct s from DonationSheet s left join fetch s.donations where s.id = :id")
    Optional<DonationSheet> findByIdWithDonations(@Param("id") String id);
}
