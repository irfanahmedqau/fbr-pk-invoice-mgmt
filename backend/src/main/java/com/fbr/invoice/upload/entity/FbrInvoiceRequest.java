package com.fbr.invoice.upload.entity;




import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "fbr_invoice_request")
public class FbrInvoiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceType;
    private LocalDate invoiceDate;
    private String sellerNTNCNIC;
    private String sellerBusinessName;
    private String sellerProvince;
    private String sellerAddress;
    private String buyerNTNCNIC;
    private String buyerBusinessName;
    private String buyerProvince;
    private String buyerAddress;
    private String buyerRegistrationType;
    private String invoiceRefNo;
    private String scenarioId;
    private String fbrInvRefNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(columnDefinition = "LONGTEXT")
    private String validationResponse;

    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<FbrItem> items;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate localDate) {
		this.invoiceDate = localDate;
	}

	public String getSellerNTNCNIC() {
		return sellerNTNCNIC;
	}

	public void setSellerNTNCNIC(String sellerNTNCNIC) {
		this.sellerNTNCNIC = sellerNTNCNIC;
	}

	public String getSellerBusinessName() {
		return sellerBusinessName;
	}

	public void setSellerBusinessName(String sellerBusinessName) {
		this.sellerBusinessName = sellerBusinessName;
	}

	public String getSellerProvince() {
		return sellerProvince;
	}

	public void setSellerProvince(String sellerProvince) {
		this.sellerProvince = sellerProvince;
	}

	public String getSellerAddress() {
		return sellerAddress;
	}

	public void setSellerAddress(String sellerAddress) {
		this.sellerAddress = sellerAddress;
	}

	public String getBuyerNTNCNIC() {
		return buyerNTNCNIC;
	}

	public void setBuyerNTNCNIC(String buyerNTNCNIC) {
		this.buyerNTNCNIC = buyerNTNCNIC;
	}

	public String getBuyerBusinessName() {
		return buyerBusinessName;
	}

	public void setBuyerBusinessName(String buyerBusinessName) {
		this.buyerBusinessName = buyerBusinessName;
	}

	public String getBuyerProvince() {
		return buyerProvince;
	}

	public void setBuyerProvince(String buyerProvince) {
		this.buyerProvince = buyerProvince;
	}

	public String getBuyerAddress() {
		return buyerAddress;
	}

	public void setBuyerAddress(String buyerAddress) {
		this.buyerAddress = buyerAddress;
	}

	public String getBuyerRegistrationType() {
		return buyerRegistrationType;
	}

	public void setBuyerRegistrationType(String buyerRegistrationType) {
		this.buyerRegistrationType = buyerRegistrationType;
	}

	public String getInvoiceRefNo() {
		return invoiceRefNo;
	}

	public void setInvoiceRefNo(String invoiceRefNo) {
		this.invoiceRefNo = invoiceRefNo;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

	public String getFbrInvRefNumber() {
		return fbrInvRefNumber;
	}

	public void setFbrInvRefNumber(String fbrInvRefNumber) {
		this.fbrInvRefNumber = fbrInvRefNumber;
	}

	public InvoiceStatus getStatus() {
		return status;
	}

	public void setStatus(InvoiceStatus status) {
		this.status = status;
	}

	public String getValidationResponse() {
		return validationResponse;
	}

	public void setValidationResponse(String validationResponse) {
		this.validationResponse = validationResponse;
	}

	public LocalDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(LocalDateTime processedAt) {
		this.processedAt = processedAt;
	}

	public List<FbrItem> getItems() {
		return items;
	}

	public void setItems(List<FbrItem> items) {
		this.items = items;
	}
    
    
    
}
