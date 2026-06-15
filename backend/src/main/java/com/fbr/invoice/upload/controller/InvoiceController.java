package com.fbr.invoice.upload.controller;

import com.fbr.invoice.upload.dto.*;
import com.fbr.invoice.upload.entity.FbrInvoiceRequest;
import com.fbr.invoice.upload.service.InvoiceService;
import com.fbr.invoice.upload.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PdfService pdfService;

    // ------------------------------------------------------------------
    // Save to local DB only
    // ------------------------------------------------------------------

    @PostMapping("/save-invoice")
    public ResponseEntity<FbrInvoiceRequest> insertInvoice(@RequestBody FbrInvoicePayload payload) {
        return ResponseEntity.ok(invoiceService.saveInvoice(payload));
    }

    // ------------------------------------------------------------------
    // Validate + channel single invoice to FBR (manual entry)
    // ------------------------------------------------------------------

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadInvoice(@RequestBody FbrInvoicePayload payload) {
        Object fbrResponse = invoiceService.uploadFromDto(payload);
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
            @RequestBody FbrInvoicePayload payload) {
        Object fbrResponse = invoiceService.resubmitInvoice(id, payload);
        return ResponseEntity.ok(fbrResponse);
    }

    // ------------------------------------------------------------------
    // Excel bulk upload
    // ------------------------------------------------------------------

    @PostMapping("/upload-excel")
    public ResponseEntity<BulkUploadResult> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sheetIndex", defaultValue = "0") int sheetIndex) throws IOException {
        log.info("Excel upload request received — file: {}, size: {} bytes, sheetIndex: {}",
                file.getOriginalFilename(), file.getSize(), sheetIndex);
        BulkUploadResult result = invoiceService.processExcelUpload(file, sheetIndex);
        log.info("Excel upload complete — total: {}, success: {}, failed: {}, skipped: {}",
                result.getTotalRows(), result.getSuccessCount(), result.getFailedCount(), result.getSkippedCount());
        return ResponseEntity.ok(result);
    }

    // ------------------------------------------------------------------
    // Post VALIDATED invoices to FBR → POSTED
    // ------------------------------------------------------------------

    @PostMapping("/post-validated")
    public ResponseEntity<BulkPostResult> postValidatedInvoices() {
        return ResponseEntity.ok(invoiceService.postValidatedInvoices());
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<Object> postInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.postInvoiceById(id));
    }

    // ------------------------------------------------------------------
    // Download PDF for a POSTED invoice
    // ------------------------------------------------------------------

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generateInvoicePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
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
