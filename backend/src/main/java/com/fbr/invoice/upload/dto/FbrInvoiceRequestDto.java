package com.fbr.invoice.upload.dto;


import java.util.List;
import java.util.Objects;




public class FbrInvoiceRequestDto {
    private String invoiceType;
    private String invoiceDate;
    private String sellerNtnCnic;
    private String sellerBusinessName;
    private String sellerProvince;
    private String sellerAddress;
    private String buyerNtnCnic;
    private String buyerBusinessName;
    private String buyerProvince;
    private String buyerAddress;
    private String buyerRegistrationType;
    private String invoiceRefNo;
    private String scenarioId;

    private List<FbrItemDto> items;

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getSellerNtnCnic() {
		return sellerNtnCnic;
	}

	public void setSellerNtnCnic(String sellerNtnCnic) {
		this.sellerNtnCnic = sellerNtnCnic;
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

	public String getBuyerNtnCnic() {
		return buyerNtnCnic;
	}

	public void setBuyerNtnCnic(String buyerNtnCnic) {
		this.buyerNtnCnic = buyerNtnCnic;
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

	public List<FbrItemDto> getItems() {
		return items;
	}

	public void setItems(List<FbrItemDto> items) {
		this.items = items;
	}

	@Override
	public int hashCode() {
		return Objects.hash(buyerAddress, buyerBusinessName, buyerNtnCnic, buyerProvince, buyerRegistrationType,
				invoiceDate, invoiceRefNo, invoiceType, items, scenarioId, sellerAddress, sellerBusinessName,
				sellerNtnCnic, sellerProvince);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FbrInvoiceRequestDto other = (FbrInvoiceRequestDto) obj;
		return Objects.equals(buyerAddress, other.buyerAddress)
				&& Objects.equals(buyerBusinessName, other.buyerBusinessName)
				&& Objects.equals(buyerNtnCnic, other.buyerNtnCnic)
				&& Objects.equals(buyerProvince, other.buyerProvince)
				&& Objects.equals(buyerRegistrationType, other.buyerRegistrationType)
				&& Objects.equals(invoiceDate, other.invoiceDate) && Objects.equals(invoiceRefNo, other.invoiceRefNo)
				&& Objects.equals(invoiceType, other.invoiceType) && Objects.equals(items, other.items)
				&& Objects.equals(scenarioId, other.scenarioId) && Objects.equals(sellerAddress, other.sellerAddress)
				&& Objects.equals(sellerBusinessName, other.sellerBusinessName)
				&& Objects.equals(sellerNtnCnic, other.sellerNtnCnic)
				&& Objects.equals(sellerProvince, other.sellerProvince);
	}
    
    
}

