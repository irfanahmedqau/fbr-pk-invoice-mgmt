package com.fbr.invoice.upload.repository;

import com.fbr.invoice.upload.entity.BuyerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerProfileRepository extends JpaRepository<BuyerProfile, Long> {

    Optional<BuyerProfile> findByNtnCnic(String ntnCnic);

    boolean existsByNtnCnic(String ntnCnic);
}
