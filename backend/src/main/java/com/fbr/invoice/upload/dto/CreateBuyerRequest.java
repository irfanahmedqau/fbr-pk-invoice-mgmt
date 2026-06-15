package com.fbr.invoice.upload.dto;

public class CreateBuyerRequest {

    private String ntnCnic;
    private String businessName;
    private String address;
    private String province;

    public String getNtnCnic() { return ntnCnic; }
    public void setNtnCnic(String ntnCnic) { this.ntnCnic = ntnCnic; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
}
