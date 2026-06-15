package com.fbr.invoice.upload.dto;

import com.fbr.invoice.upload.entity.FbrInvoiceRequest;
import com.fbr.invoice.upload.entity.InvoiceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InvoiceSummaryDto {

    private Long id;
    private String invoiceRefNo;
    private String invoiceType;
    private LocalDate invoiceDate;
    private String sellerNTNCNIC;
    private String sellerBusinessName;
    private String buyerNTNCNIC;
    private String buyerBusinessName;
    private String buyerProvince;
    private String scenarioId;
    private InvoiceStatus status;
    private LocalDateTime processedAt;
    private String validationResponse;
    private String fbrInvRefNumber;

    public static InvoiceSummaryDto from(FbrInvoiceRequest entity) {
        InvoiceSummaryDto dto = new InvoiceSummaryDto();
        dto.id                  = entity.getId();
        dto.invoiceRefNo        = entity.getInvoiceRefNo();
        dto.invoiceType         = entity.getInvoiceType();
        dto.invoiceDate         = entity.getInvoiceDate();
        dto.sellerNTNCNIC       = entity.getSellerNTNCNIC();
        dto.sellerBusinessName  = entity.getSellerBusinessName();
        dto.buyerNTNCNIC        = entity.getBuyerNTNCNIC();
        dto.buyerBusinessName   = entity.getBuyerBusinessName();
        dto.buyerProvince       = entity.getBuyerProvince();
        dto.scenarioId          = entity.getScenarioId();
        dto.status              = entity.getStatus();
        dto.processedAt         = entity.getProcessedAt();
        dto.validationResponse  = entity.getValidationResponse();
        dto.fbrInvRefNumber     = entity.getFbrInvRefNumber();
        return dto;
    }

    public Long getId() { return id; }
    public String getInvoiceRefNo() { return invoiceRefNo; }
    public String getInvoiceType() { return invoiceType; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public String getSellerNTNCNIC() { return sellerNTNCNIC; }
    public String getSellerBusinessName() { return sellerBusinessName; }
    public String getBuyerNTNCNIC() { return buyerNTNCNIC; }
    public String getBuyerBusinessName() { return buyerBusinessName; }
    public String getBuyerProvince() { return buyerProvince; }
    public String getScenarioId() { return scenarioId; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public String getValidationResponse() { return validationResponse; }
    public String getFbrInvRefNumber() { return fbrInvRefNumber; }
}
