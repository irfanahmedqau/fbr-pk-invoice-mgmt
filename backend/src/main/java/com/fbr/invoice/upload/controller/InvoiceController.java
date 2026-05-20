package com.fbr.invoice.upload.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbr.invoice.upload.dto.*;
import com.fbr.invoice.upload.entity.FbrInvoiceRequest;
import com.fbr.invoice.upload.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    // ------------------------------------------------------------------
    // Save to local DB only
    // ------------------------------------------------------------------

    @PostMapping("/save-invoice")
    public ResponseEntity<FbrInvoiceRequest> insertInvoice(@RequestBody FbrInvoiceRequestDto dto) {
        try {
            System.out.println("Received DTO: " + new ObjectMapper().writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(invoiceService.saveInvoice(dto));
    }

    // ------------------------------------------------------------------
    // Validate + channel single invoice to FBR (manual entry)
    // ------------------------------------------------------------------

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadInvoice(@RequestBody FbrInvoiceRequestDto dto) {
        Object fbrResponse = invoiceService.uploadFromDto(dto);
        return ResponseEntity.ok(fbrResponse);
    }

    // ------------------------------------------------------------------
    // Validate payload already in FBR format
    // ------------------------------------------------------------------

    @PostMapping("/validate")
    public ResponseEntity<Object> validateInvoice(@RequestBody FbrInvoicePayload payload) {
        return ResponseEntity.ok(invoiceService.validateAndUpload(payload));
    }

    // ------------------------------------------------------------------
    // Resubmit a FAILED invoice with corrected data
    // ------------------------------------------------------------------

    @PutMapping("/{id}/resubmit")
    public ResponseEntity<Object> resubmitInvoice(
            @PathVariable Long id,
            @RequestBody FbrInvoiceRequestDto dto) {
        Object fbrResponse = invoiceService.resubmitInvoice(id, dto);
        return ResponseEntity.ok(fbrResponse);
    }

    // ------------------------------------------------------------------
    // Excel bulk upload
    // ------------------------------------------------------------------

    @PostMapping("/upload-excel")
    public ResponseEntity<BulkUploadResult> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sheetIndex", defaultValue = "0") int sheetIndex) throws IOException {
        return ResponseEntity.ok(invoiceService.processExcelUpload(file, sheetIndex));
    }

    // ------------------------------------------------------------------
    // Query — list + get by ID (for status grid and edit/resubmit)
    // ------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<InvoiceSummaryDto>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FbrInvoiceRequest> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }
}
