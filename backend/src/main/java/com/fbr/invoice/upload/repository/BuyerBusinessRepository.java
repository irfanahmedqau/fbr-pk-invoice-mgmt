package com.fbr.invoice.upload.repository;

import com.fbr.invoice.upload.entity.BuyerBusiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerBusinessRepository extends JpaRepository<BuyerBusiness, Long> {

    List<BuyerBusiness> findByBuyerProfileId(Long profileId);

    Optional<BuyerBusiness> findByIdAndBuyerProfileNtnCnic(Long id, String ntnCnic);
}
