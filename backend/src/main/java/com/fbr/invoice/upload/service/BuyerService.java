package com.fbr.invoice.upload.service;

import com.fbr.invoice.upload.dto.BuyerBusinessDto;
import com.fbr.invoice.upload.dto.BuyerProfileDto;
import com.fbr.invoice.upload.dto.CreateBuyerRequest;
import com.fbr.invoice.upload.entity.BuyerBusiness;
import com.fbr.invoice.upload.entity.BuyerProfile;
import com.fbr.invoice.upload.repository.BuyerBusinessRepository;
import com.fbr.invoice.upload.repository.BuyerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BuyerService {

    @Autowired
    private BuyerProfileRepository profileRepo;

    @Autowired
    private BuyerBusinessRepository businessRepo;

    @Autowired
    private FbrApiService fbrApiService;

    // ------------------------------------------------------------------
    // Lookup
    // ------------------------------------------------------------------

    public Optional<BuyerProfileDto> findByNtnCnic(String ntnCnic) {
        return profileRepo.findByNtnCnic(ntnCnic).map(this::toDto);
    }

    // ------------------------------------------------------------------
    // Create buyer with first business
    // ------------------------------------------------------------------

    @Transactional
    public BuyerProfileDto createBuyer(CreateBuyerRequest request) {
        if (profileRepo.existsByNtnCnic(request.getNtnCnic())) {
            throw new IllegalArgumentException("Buyer with NTN/CNIC " + request.getNtnCnic() + " already exists");
        }

        BuyerProfile profile = new BuyerProfile();
        profile.setNtnCnic(request.getNtnCnic());

        // Attempt to fetch reg type from FBR on creation; silently ignore if FBR is unavailable
        try {
            Object fbrResponse = fbrApiService.getBuyerRegType(request.getNtnCnic());
            profile.setRegType(extractRegType(fbrResponse));
            profile.setRegTypeLastChecked(LocalDateTime.now());
        } catch (Exception ignored) {}

        profileRepo.save(profile);

        BuyerBusiness business = new BuyerBusiness();
        business.setBuyerProfile(profile);
        business.setBusinessName(request.getBusinessName());
        business.setAddress(request.getAddress());
        business.setProvince(request.getProvince());
        business.setDefault(true);
        businessRepo.save(business);

        return toDto(profileRepo.findByNtnCnic(profile.getNtnCnic()).orElseThrow());
    }

    // ------------------------------------------------------------------
    // Add another business to existing buyer
    // ------------------------------------------------------------------

    @Transactional
    public BuyerProfileDto addBusiness(String ntnCnic, BuyerBusinessDto dto) {
        BuyerProfile profile = profileRepo.findByNtnCnic(ntnCnic)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + ntnCnic));

        if (dto.isDefault()) {
            clearDefaultFlag(profile.getId());
        }

        BuyerBusiness business = new BuyerBusiness();
        business.setBuyerProfile(profile);
        business.setBusinessName(dto.getBusinessName());
        business.setAddress(dto.getAddress());
        business.setProvince(dto.getProvince());
        business.setDefault(dto.isDefault());
        businessRepo.save(business);

        return toDto(profileRepo.findByNtnCnic(ntnCnic).orElseThrow());
    }

    // ------------------------------------------------------------------
    // Update an existing business entry
    // ------------------------------------------------------------------

    @Transactional
    public BuyerProfileDto updateBusiness(String ntnCnic, Long businessId, BuyerBusinessDto dto) {
        BuyerBusiness business = businessRepo.findByIdAndBuyerProfileNtnCnic(businessId, ntnCnic)
                .orElseThrow(() -> new IllegalArgumentException("Business not found for buyer: " + ntnCnic));

        if (dto.isDefault() && !business.isDefault()) {
            clearDefaultFlag(business.getBuyerProfile().getId());
        }

        business.setBusinessName(dto.getBusinessName());
        business.setAddress(dto.getAddress());
        business.setProvince(dto.getProvince());
        business.setDefault(dto.isDefault());
        businessRepo.save(business);

        return toDto(profileRepo.findByNtnCnic(ntnCnic).orElseThrow());
    }

    // ------------------------------------------------------------------
    // Delete a business entry
    // ------------------------------------------------------------------

    @Transactional
    public BuyerProfileDto deleteBusiness(String ntnCnic, Long businessId) {
        BuyerBusiness business = businessRepo.findByIdAndBuyerProfileNtnCnic(businessId, ntnCnic)
                .orElseThrow(() -> new IllegalArgumentException("Business not found for buyer: " + ntnCnic));
        businessRepo.delete(business);
        return toDto(profileRepo.findByNtnCnic(ntnCnic).orElseThrow());
    }

    // ------------------------------------------------------------------
    // Refresh reg type from FBR and persist
    // ------------------------------------------------------------------

    @Transactional
    public BuyerProfileDto refreshRegType(String ntnCnic) {
        BuyerProfile profile = profileRepo.findByNtnCnic(ntnCnic)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + ntnCnic));

        Object fbrResponse = fbrApiService.getBuyerRegType(ntnCnic);
        profile.setRegType(extractRegType(fbrResponse));
        profile.setRegTypeLastChecked(LocalDateTime.now());
        profileRepo.save(profile);

        return toDto(profileRepo.findByNtnCnic(ntnCnic).orElseThrow());
    }

    // ------------------------------------------------------------------
    // List all buyers (for management UI)
    // ------------------------------------------------------------------

    public List<BuyerProfileDto> listAll() {
        return profileRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private void clearDefaultFlag(Long profileId) {
        businessRepo.findByBuyerProfileId(profileId).forEach(b -> {
            if (b.isDefault()) {
                b.setDefault(false);
                businessRepo.save(b);
            }
        });
    }

    private BuyerProfileDto toDto(BuyerProfile profile) {
        BuyerProfileDto dto = new BuyerProfileDto();
        dto.setId(profile.getId());
        dto.setNtnCnic(profile.getNtnCnic());
        dto.setRegType(profile.getRegType());
        dto.setRegTypeLastChecked(profile.getRegTypeLastChecked());
        dto.setBusinesses(
            businessRepo.findByBuyerProfileId(profile.getId()).stream()
                .map(this::toBusinessDto)
                .collect(Collectors.toList())
        );
        return dto;
    }

    private BuyerBusinessDto toBusinessDto(BuyerBusiness b) {
        BuyerBusinessDto dto = new BuyerBusinessDto();
        dto.setId(b.getId());
        dto.setBusinessName(b.getBusinessName());
        dto.setAddress(b.getAddress());
        dto.setProvince(b.getProvince());
        dto.setDefault(b.isDefault());
        return dto;
    }

    private String extractRegType(Object fbrResponse) {
        if (fbrResponse == null) return null;
        // FBR returns a JSON object; store as string for flexibility
        return fbrResponse.toString();
    }
}
