package com.temple.donation.repository;

import com.temple.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, String> {

    @Query("select d from Donation d join fetch d.sheet")
    List<Donation> findAllWithSheet();

    @Query("select coalesce(sum(d.amount), 0) from Donation d")
    BigDecimal sumAmount();
}
