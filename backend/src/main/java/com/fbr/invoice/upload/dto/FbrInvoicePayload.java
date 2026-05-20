package com.fbr.invoice.upload.dto;

import java.util.List;

/**
 * Invoice payload that matches FBR API field names exactly.
 * Used for both validate and channeling calls.
 * Field names intentionally use FBR casing (sellerNTNCNIC, buyerNTNCNIC, etc.)
 */
public class FbrInvoicePayload {

    private String invoiceType;
    private String invoiceDate;
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
    private List<FbrItemPayload> items;

    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }

    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getSellerNTNCNIC() { return sellerNTNCNIC; }
    public void setSellerNTNCNIC(String sellerNTNCNIC) { this.sellerNTNCNIC = sellerNTNCNIC; }

    public String getSellerBusinessName() { return sellerBusinessName; }
    public void setSellerBusinessName(String sellerBusinessName) { this.sellerBusinessName = sellerBusinessName; }

    public String getSellerProvince() { return sellerProvince; }
    public void setSellerProvince(String sellerProvince) { this.sellerProvince = sellerProvince; }

    public String getSellerAddress() { return sellerAddress; }
    public void setSellerAddress(String sellerAddress) { this.sellerAddress = sellerAddress; }

    public String getBuyerNTNCNIC() { return buyerNTNCNIC; }
    public void setBuyerNTNCNIC(String buyerNTNCNIC) { this.buyerNTNCNIC = buyerNTNCNIC; }

    public String getBuyerBusinessName() { return buyerBusinessName; }
    public void setBuyerBusinessName(String buyerBusinessName) { this.buyerBusinessName = buyerBusinessName; }

    public String getBuyerProvince() { return buyerProvince; }
    public void setBuyerProvince(String buyerProvince) { this.buyerProvince = buyerProvince; }

    public String getBuyerAddress() { return buyerAddress; }
    public void setBuyerAddress(String buyerAddress) { this.buyerAddress = buyerAddress; }

    public String getBuyerRegistrationType() { return buyerRegistrationType; }
    public void setBuyerRegistrationType(String buyerRegistrationType) { this.buyerRegistrationType = buyerRegistrationType; }

    public String getInvoiceRefNo() { return invoiceRefNo; }
    public void setInvoiceRefNo(String invoiceRefNo) { this.invoiceRefNo = invoiceRefNo; }

    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }

    public List<FbrItemPayload> getItems() { return items; }
    public void setItems(List<FbrItemPayload> items) { this.items = items; }
}
