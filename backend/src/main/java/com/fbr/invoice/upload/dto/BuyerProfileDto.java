package com.fbr.invoice.upload.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BuyerProfileDto {

    private Long id;
    private String ntnCnic;
    private String regType;
    private LocalDateTime regTypeLastChecked;
    private List<BuyerBusinessDto> businesses;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNtnCnic() { return ntnCnic; }
    public void setNtnCnic(String ntnCnic) { this.ntnCnic = ntnCnic; }

    public String getRegType() { return regType; }
    public void setRegType(String regType) { this.regType = regType; }

    public LocalDateTime getRegTypeLastChecked() { return regTypeLastChecked; }
    public void setRegTypeLastChecked(LocalDateTime regTypeLastChecked) { this.regTypeLastChecked = regTypeLastChecked; }

    public List<BuyerBusinessDto> getBusinesses() { return businesses; }
    public void setBusinesses(List<BuyerBusinessDto> businesses) { this.businesses = businesses; }
}
