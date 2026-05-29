package com.fbr.invoice.upload.repository;

import com.fbr.invoice.upload.entity.FbrInvoiceRequest;
import com.fbr.invoice.upload.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FbrInvoiceRequestRepository extends JpaRepository<FbrInvoiceRequest, Long> {

    Optional<FbrInvoiceRequest> findFirstByInvoiceRefNoOrderByIdDesc(String invoiceRefNo);

    boolean existsByInvoiceRefNoAndStatus(String invoiceRefNo, InvoiceStatus status);

    List<FbrInvoiceRequest> findAllByOrderByProcessedAtDesc();

    List<FbrInvoiceRequest> findByStatus(InvoiceStatus status);
}
