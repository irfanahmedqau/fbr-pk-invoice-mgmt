package com.fbr.invoice.upload.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buyer_profile")
public class BuyerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ntn_cnic", unique = true, nullable = false, length = 20)
    private String ntnCnic;

    private String regType;

    private LocalDateTime regTypeLastChecked;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "buyerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuyerBusiness> businesses = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNtnCnic() { return ntnCnic; }
    public void setNtnCnic(String ntnCnic) { this.ntnCnic = ntnCnic; }

    public String getRegType() { return regType; }
    public void setRegType(String regType) { this.regType = regType; }

    public LocalDateTime getRegTypeLastChecked() { return regTypeLastChecked; }
    public void setRegTypeLastChecked(LocalDateTime regTypeLastChecked) { this.regTypeLastChecked = regTypeLastChecked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<BuyerBusiness> getBusinesses() { return businesses; }
    public void setBusinesses(List<BuyerBusiness> businesses) { this.businesses = businesses; }
}
