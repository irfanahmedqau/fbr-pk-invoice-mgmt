package com.fbr.invoice.upload.controller;

import com.fbr.invoice.upload.dto.BuyerBusinessDto;
import com.fbr.invoice.upload.dto.BuyerProfileDto;
import com.fbr.invoice.upload.dto.CreateBuyerRequest;
import com.fbr.invoice.upload.service.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyers")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    // GET /api/buyers — list all buyers
    @GetMapping
    public ResponseEntity<List<BuyerProfileDto>> listAll() {
        return ResponseEntity.ok(buyerService.listAll());
    }

    // GET /api/buyers/{ntnCnic} — lookup by NTN/CNIC; 404 if not found (triggers popup in UI)
    @GetMapping("/{ntnCnic}")
    public ResponseEntity<BuyerProfileDto> getByNtnCnic(@PathVariable String ntnCnic) {
        return buyerService.findByNtnCnic(ntnCnic)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/buyers — create new buyer with first business
    @PostMapping
    public ResponseEntity<BuyerProfileDto> createBuyer(@RequestBody CreateBuyerRequest request) {
        try {
            return ResponseEntity.ok(buyerService.createBuyer(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // POST /api/buyers/{ntnCnic}/businesses — add a business to existing buyer
    @PostMapping("/{ntnCnic}/businesses")
    public ResponseEntity<BuyerProfileDto> addBusiness(
            @PathVariable String ntnCnic,
            @RequestBody BuyerBusinessDto dto) {
        try {
            return ResponseEntity.ok(buyerService.addBusiness(ntnCnic, dto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/buyers/{ntnCnic}/businesses/{id} — update a business entry
    @PutMapping("/{ntnCnic}/businesses/{id}")
    public ResponseEntity<BuyerProfileDto> updateBusiness(
            @PathVariable String ntnCnic,
            @PathVariable Long id,
            @RequestBody BuyerBusinessDto dto) {
        try {
            return ResponseEntity.ok(buyerService.updateBusiness(ntnCnic, id, dto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/buyers/{ntnCnic}/businesses/{id} — remove a business entry
    @DeleteMapping("/{ntnCnic}/businesses/{id}")
    public ResponseEntity<BuyerProfileDto> deleteBusiness(
            @PathVariable String ntnCnic,
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(buyerService.deleteBusiness(ntnCnic, id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/buyers/{ntnCnic}/refresh-reg-type — re-fetch reg type from FBR and store
    @PostMapping("/{ntnCnic}/refresh-reg-type")
    public ResponseEntity<BuyerProfileDto> refreshRegType(@PathVariable String ntnCnic) {
        try {
            return ResponseEntity.ok(buyerService.refreshRegType(ntnCnic));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
