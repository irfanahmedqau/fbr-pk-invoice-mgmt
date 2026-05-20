package com.fbr.invoice.upload.controller;

import com.fbr.invoice.upload.service.FbrApiService;
import com.fbr.invoice.upload.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Proxy controller — exposes FBR reference/lookup endpoints to the Angular UI.
 * The Angular app calls these instead of FBR directly, keeping the API key server-side.
 */
@RestController
@RequestMapping("/api/fbr")
public class FbrController {

    @Autowired
    private FbrApiService fbrApiService;

    @Autowired
    private InvoiceService invoiceService;

    // ------------------------------------------------------------------
    // Reference data (populate dropdowns in the UI)
    // ------------------------------------------------------------------

    @GetMapping("/provinces")
    public ResponseEntity<Object> getProvinces() {
        return ResponseEntity.ok(fbrApiService.getProvinces());
    }

    @GetMapping("/uom")
    public ResponseEntity<Object> getUom() {
        return ResponseEntity.ok(fbrApiService.getUom());
    }

    @GetMapping("/transaction-types")
    public ResponseEntity<Object> getTransactionTypes() {
        return ResponseEntity.ok(fbrApiService.getTransactionTypes());
    }

    @GetMapping("/doc-types")
    public ResponseEntity<Object> getDocTypes() {
        return ResponseEntity.ok(fbrApiService.getDocTypes());
    }

    @GetMapping("/hs-codes")
    public ResponseEntity<Object> getHsCodes() {
        return ResponseEntity.ok(fbrApiService.getHsCodes());
    }

    // ------------------------------------------------------------------
    // Buyer validation
    // ------------------------------------------------------------------

    @GetMapping("/buyer-atl")
    public ResponseEntity<Object> getBuyerAtl(
            @RequestParam String ntn,
            @RequestParam String date) {
        return ResponseEntity.ok(fbrApiService.getBuyerAtl(ntn, date));
    }

    @GetMapping("/buyer-reg-type")
    public ResponseEntity<Object> getBuyerRegType(@RequestParam String ntn) {
        return ResponseEntity.ok(fbrApiService.getBuyerRegType(ntn));
    }

    // ------------------------------------------------------------------
    // Excel sheet names — called before upload so user can select sheet
    // ------------------------------------------------------------------

    @PostMapping("/excel-sheets")
    public ResponseEntity<List<String>> getExcelSheets(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(invoiceService.getExcelSheetNames(file));
    }
}
