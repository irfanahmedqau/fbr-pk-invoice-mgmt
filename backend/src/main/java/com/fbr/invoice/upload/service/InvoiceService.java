package com.fbr.invoice.upload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbr.invoice.upload.dto.*;
import com.fbr.invoice.upload.entity.FbrInvoiceRequest;
import com.fbr.invoice.upload.entity.FbrItem;
import com.fbr.invoice.upload.entity.InvoiceStatus;
import com.fbr.invoice.upload.repository.FbrInvoiceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    @Autowired private FbrInvoiceRequestRepository invoiceRepo;
    @Autowired private FbrApiService fbrApiService;
    @Autowired private ExcelParserService excelParserService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ------------------------------------------------------------------
    // Save invoice to local DB only (no FBR call)
    // ------------------------------------------------------------------

    public FbrInvoiceRequest saveInvoice(FbrInvoiceRequestDto dto) {
        FbrInvoiceRequest invoice = findOrCreate(dto.getInvoiceRefNo());
        populateFromDto(invoice, dto);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setProcessedAt(LocalDateTime.now());
        return invoiceRepo.save(invoice);
    }

    // ------------------------------------------------------------------
    // Validate + channel single invoice from UI (manual entry)
    // Duplicate rule: POSTED invoices cannot be resubmitted via this path.
    // ------------------------------------------------------------------

    public Object uploadFromDto(FbrInvoiceRequestDto dto) {
        guardAgainstDuplicate(dto.getInvoiceRefNo());

        FbrInvoiceRequest invoice = findOrCreate(dto.getInvoiceRefNo());
        populateFromDto(invoice, dto);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setProcessedAt(LocalDateTime.now());
        invoice = invoiceRepo.save(invoice);

        return validateAndUpdateStatus(invoice, toFbrPayload(dto));
    }

    // ------------------------------------------------------------------
    // Resubmit a FAILED invoice — allowed to update data + retry FBR
    // ------------------------------------------------------------------

    public Object resubmitInvoice(Long id, FbrInvoiceRequestDto dto) {
        FbrInvoiceRequest invoice = invoiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));

        if (invoice.getStatus() != InvoiceStatus.FAILED) {
            throw new RuntimeException(
                "Only FAILED invoices can be resubmitted. Current status: " + invoice.getStatus());
        }

        populateFromDto(invoice, dto);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setProcessedAt(LocalDateTime.now());
        invoice = invoiceRepo.save(invoice);

        return validateAndUpdateStatus(invoice, toFbrPayload(dto));
    }

    // ------------------------------------------------------------------
    // Direct FBR payload validate (used by /validate endpoint)
    // ------------------------------------------------------------------

    public Object validateAndUpload(FbrInvoicePayload payload) {
        return fbrApiService.validateInvoice(payload);
    }

    // ------------------------------------------------------------------
    // Excel bulk upload — skip POSTED duplicates, continue on FAILED rows
    // ------------------------------------------------------------------

    public BulkUploadResult processExcelUpload(MultipartFile file, int sheetIndex) throws IOException {
        List<FbrInvoicePayload> invoices = excelParserService.parseExcel(file, sheetIndex);
        List<ExcelRowResult> results = new ArrayList<>();
        int success = 0, failed = 0, skipped = 0;

        for (int i = 0; i < invoices.size(); i++) {
            FbrInvoicePayload payload = invoices.get(i);
            int rowNumber = i + 2;
            ExcelRowResult result = new ExcelRowResult();
            result.setRowNumber(rowNumber);
            result.setInvoiceRefNo(payload.getInvoiceRefNo());

            // Duplicate guard — skip already POSTED invoices
            if (invoiceRepo.existsByInvoiceRefNoAndStatus(payload.getInvoiceRefNo(), InvoiceStatus.POSTED)) {
                result.setStatus("SKIPPED_DUPLICATE");
                result.setMessage("Invoice already posted to FBR — skipped");
                skipped++;
                results.add(result);
                continue;
            }

            FbrInvoiceRequest invoice = findOrCreate(payload.getInvoiceRefNo());
            populateFromPayload(invoice, payload);
            invoice.setStatus(InvoiceStatus.PENDING);
            invoice.setProcessedAt(LocalDateTime.now());
            invoice = invoiceRepo.save(invoice);

            try {
                Object fbrResponse = fbrApiService.validateInvoice(payload);
                String statusCode = extractStatusCode(fbrResponse);

                if ("00".equals(statusCode)) {
                    invoice.setStatus(InvoiceStatus.VALIDATED);
                    result.setStatus("SUCCESS");
                    result.setMessage("Validated by FBR");
                    success++;
                } else {
                    invoice.setStatus(InvoiceStatus.FAILED);
                    result.setStatus("FAILED");
                    result.setMessage("FBR validation failed — statusCode: " + statusCode);
                    failed++;
                }
                invoice.setValidationResponse(objectMapper.writeValueAsString(fbrResponse));
                result.setFbrResponse(fbrResponse);
            } catch (Exception ex) {
                invoice.setStatus(InvoiceStatus.FAILED);
                invoice.setValidationResponse(ex.getMessage());
                result.setStatus("FAILED");
                result.setMessage(ex.getMessage());
                failed++;
            }

            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepo.save(invoice);
            results.add(result);
        }

        return new BulkUploadResult(invoices.size(), success, failed, skipped, results);
    }

    public List<String> getExcelSheetNames(MultipartFile file) throws IOException {
        return excelParserService.getSheetNames(file);
    }

    // ------------------------------------------------------------------
    // Query — list all invoices (summary) + get by ID
    // ------------------------------------------------------------------

    public List<InvoiceSummaryDto> getAllInvoices() {
        return invoiceRepo.findAllByOrderByProcessedAtDesc()
                .stream()
                .map(InvoiceSummaryDto::from)
                .collect(Collectors.toList());
    }

    public FbrInvoiceRequest getInvoiceById(Long id) {
        return invoiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
    }

    // ------------------------------------------------------------------
    // Post all VALIDATED invoices to FBR → POSTED
    // ------------------------------------------------------------------

    public BulkPostResult postValidatedInvoices() {
        List<FbrInvoiceRequest> validated = invoiceRepo.findByStatus(InvoiceStatus.VALIDATED);
        List<ExcelRowResult> results = new ArrayList<>();
        int posted = 0, failed = 0;

        for (FbrInvoiceRequest invoice : validated) {
            ExcelRowResult result = new ExcelRowResult();
            result.setInvoiceRefNo(invoice.getInvoiceRefNo());

            try {
                Object fbrResponse = fbrApiService.postInvoice(entityToPayload(invoice));
                String statusCode = extractStatusCode(fbrResponse);

                if ("00".equals(statusCode)) {
                    invoice.setStatus(InvoiceStatus.POSTED);
                    result.setStatus("POSTED");
                    result.setMessage("Posted to FBR successfully");
                    posted++;
                } else {
                    invoice.setStatus(InvoiceStatus.FAILED);
                    result.setStatus("FAILED");
                    result.setMessage("FBR post failed — statusCode: " + statusCode);
                    failed++;
                }
                invoice.setValidationResponse(objectMapper.writeValueAsString(fbrResponse));
                result.setFbrResponse(fbrResponse);
            } catch (Exception ex) {
                invoice.setStatus(InvoiceStatus.FAILED);
                invoice.setValidationResponse(ex.getMessage());
                result.setStatus("FAILED");
                result.setMessage(ex.getMessage());
                failed++;
            }

            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepo.save(invoice);
            results.add(result);
        }

        return new BulkPostResult(validated.size(), posted, failed, results);
    }

    // ------------------------------------------------------------------
    // Post a single VALIDATED invoice to FBR → POSTED
    // ------------------------------------------------------------------

    public Object postInvoiceById(Long id) {
        FbrInvoiceRequest invoice = invoiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));

        if (invoice.getStatus() != InvoiceStatus.VALIDATED) {
            throw new RuntimeException(
                "Only VALIDATED invoices can be posted. Current status: " + invoice.getStatus());
        }

        try {
            Object fbrResponse = fbrApiService.postInvoice(entityToPayload(invoice));
            String statusCode = extractStatusCode(fbrResponse);

            invoice.setStatus("00".equals(statusCode) ? InvoiceStatus.POSTED : InvoiceStatus.FAILED);
            invoice.setValidationResponse(toJson(fbrResponse));
            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepo.save(invoice);
            return fbrResponse;
        } catch (Exception ex) {
            invoice.setStatus(InvoiceStatus.FAILED);
            invoice.setValidationResponse(ex.getMessage());
            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepo.save(invoice);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private Object validateAndUpdateStatus(FbrInvoiceRequest invoice, FbrInvoicePayload payload) {
        try {
            Object fbrResponse = fbrApiService.validateInvoice(payload);
            String statusCode = extractStatusCode(fbrResponse);

            invoice.setStatus("00".equals(statusCode) ? InvoiceStatus.VALIDATED : InvoiceStatus.FAILED);
            invoice.setValidationResponse(toJson(fbrResponse));
            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepo.save(invoice);
            return fbrResponse;
        } catch (Exception ex) {
            invoice.setStatus(InvoiceStatus.FAILED);
            invoice.setValidationResponse(ex.getMessage());
            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepo.save(invoice);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void guardAgainstDuplicate(String invoiceRefNo) {
        if (invoiceRepo.existsByInvoiceRefNoAndStatus(invoiceRefNo, InvoiceStatus.POSTED)) {
            throw new RuntimeException(
                "Invoice '" + invoiceRefNo + "' has already been posted to FBR and cannot be resubmitted.");
        }
    }

    private FbrInvoiceRequest findOrCreate(String invoiceRefNo) {
        return invoiceRepo.findFirstByInvoiceRefNoOrderByIdDesc(invoiceRefNo)
                .orElse(new FbrInvoiceRequest());
    }

    private void populateFromDto(FbrInvoiceRequest invoice, FbrInvoiceRequestDto dto) {
        invoice.setInvoiceType(dto.getInvoiceType());
        if (dto.getInvoiceDate() != null && !dto.getInvoiceDate().isEmpty()) {
            invoice.setInvoiceDate(LocalDate.parse(dto.getInvoiceDate()));
        }
        invoice.setSellerNTNCNIC(dto.getSellerNtnCnic());
        invoice.setSellerBusinessName(dto.getSellerBusinessName());
        invoice.setSellerProvince(dto.getSellerProvince());
        invoice.setSellerAddress(dto.getSellerAddress());
        invoice.setBuyerNTNCNIC(dto.getBuyerNtnCnic());
        invoice.setBuyerBusinessName(dto.getBuyerBusinessName());
        invoice.setBuyerProvince(dto.getBuyerProvince());
        invoice.setBuyerAddress(dto.getBuyerAddress());
        invoice.setBuyerRegistrationType(dto.getBuyerRegistrationType());
        invoice.setInvoiceRefNo(dto.getInvoiceRefNo());
        invoice.setScenarioId(dto.getScenarioId());

        List<FbrItemDto> dtoItems = dto.getItems() != null ? dto.getItems() : List.of();
        List<FbrItem> items = dtoItems.stream().map(i -> {
            FbrItem item = new FbrItem();
            item.setHsCode(i.getHsCode());
            item.setProductDescription(i.getProductDescription());
            item.setRate(i.getRate());
            item.setUoM(i.getUom());
            item.setQuantity(i.getQuantity());
            item.setTotalValues(i.getTotalValue());
            item.setValueSalesExcludingST(i.getValueSalesExcludingSt());
            item.setFixedNotifiedValueOrRetailPrice(i.getFixedNotifiedValueOrRetailPrice());
            item.setSalesTaxApplicable(i.getSalesTaxApplicable());
            item.setSalesTaxWithheldAtSource(i.getSalesTaxWithheldAtSource());
            item.setExtraTax(i.getExtraTax());
            item.setFurtherTax(i.getFurtherTax());
            item.setSroScheduleNo(i.getSroScheduleNo());
            item.setFedPayable(i.getFedPayable());
            item.setDiscount(i.getDiscount());
            item.setSaleType(i.getSaleType());
            item.setSroItemSerialNo(i.getSroItemSerialNo());
            item.setInvoice(invoice);
            return item;
        }).collect(Collectors.toList());

        if (invoice.getItems() != null) {
            invoice.getItems().clear();
            invoice.getItems().addAll(items);
        } else {
            invoice.setItems(items);
        }
    }

    private void populateFromPayload(FbrInvoiceRequest invoice, FbrInvoicePayload payload) {
        invoice.setInvoiceType(payload.getInvoiceType());
        if (payload.getInvoiceDate() != null && !payload.getInvoiceDate().isEmpty()) {
            invoice.setInvoiceDate(LocalDate.parse(payload.getInvoiceDate()));
        }
        invoice.setSellerNTNCNIC(payload.getSellerNTNCNIC());
        invoice.setSellerBusinessName(payload.getSellerBusinessName());
        invoice.setSellerProvince(payload.getSellerProvince());
        invoice.setSellerAddress(payload.getSellerAddress());
        invoice.setBuyerNTNCNIC(payload.getBuyerNTNCNIC());
        invoice.setBuyerBusinessName(payload.getBuyerBusinessName());
        invoice.setBuyerProvince(payload.getBuyerProvince());
        invoice.setBuyerAddress(payload.getBuyerAddress());
        invoice.setBuyerRegistrationType(payload.getBuyerRegistrationType());
        invoice.setInvoiceRefNo(payload.getInvoiceRefNo());
        invoice.setScenarioId(payload.getScenarioId());

        List<FbrItem> items = payload.getItems().stream().map(i -> {
            FbrItem item = new FbrItem();
            item.setHsCode(i.getHsCode());
            item.setProductDescription(i.getProductDescription());
            item.setRate(i.getRate());
            item.setUoM(i.getUoM());
            item.setQuantity(i.getQuantity());
            item.setTotalValues(i.getTotalValues());
            item.setValueSalesExcludingST(i.getValueSalesExcludingST());
            item.setFixedNotifiedValueOrRetailPrice(i.getFixedNotifiedValueOrRetailPrice());
            item.setSalesTaxApplicable(i.getSalesTaxApplicable());
            item.setSalesTaxWithheldAtSource(i.getSalesTaxWithheldAtSource());
            item.setExtraTax(i.getExtraTax());
            item.setFurtherTax(i.getFurtherTax());
            item.setSroScheduleNo(i.getSroScheduleNo());
            item.setFedPayable(i.getFedPayable());
            item.setDiscount(i.getDiscount());
            item.setSaleType(i.getSaleType());
            item.setSroItemSerialNo(i.getSroItemSerialNo());
            item.setInvoice(invoice);
            return item;
        }).collect(Collectors.toList());

        if (invoice.getItems() != null) {
            invoice.getItems().clear();
            invoice.getItems().addAll(items);
        } else {
            invoice.setItems(items);
        }
    }

    private FbrInvoicePayload toFbrPayload(FbrInvoiceRequestDto dto) {
        FbrInvoicePayload payload = new FbrInvoicePayload();
        payload.setInvoiceType(dto.getInvoiceType());
        payload.setInvoiceDate(dto.getInvoiceDate());
        payload.setSellerNTNCNIC(dto.getSellerNtnCnic());
        payload.setSellerBusinessName(dto.getSellerBusinessName());
        payload.setSellerProvince(dto.getSellerProvince());
        payload.setSellerAddress(dto.getSellerAddress());
        payload.setBuyerNTNCNIC(dto.getBuyerNtnCnic());
        payload.setBuyerBusinessName(dto.getBuyerBusinessName());
        payload.setBuyerProvince(dto.getBuyerProvince());
        payload.setBuyerAddress(dto.getBuyerAddress());
        payload.setBuyerRegistrationType(dto.getBuyerRegistrationType());
        payload.setInvoiceRefNo(dto.getInvoiceRefNo());
        payload.setScenarioId(dto.getScenarioId());

        List<FbrItemPayload> items = dto.getItems().stream().map(i -> {
            FbrItemPayload item = new FbrItemPayload();
            item.setHsCode(i.getHsCode());
            item.setProductDescription(i.getProductDescription());
            item.setRate(i.getRate());
            item.setUoM(i.getUom());
            item.setQuantity(i.getQuantity());
            item.setTotalValues(i.getTotalValue());
            item.setValueSalesExcludingST(i.getValueSalesExcludingSt());
            item.setFixedNotifiedValueOrRetailPrice(i.getFixedNotifiedValueOrRetailPrice());
            item.setSalesTaxApplicable(i.getSalesTaxApplicable());
            item.setSalesTaxWithheldAtSource(i.getSalesTaxWithheldAtSource());
            item.setExtraTax(i.getExtraTax());
            item.setFurtherTax(i.getFurtherTax());
            item.setSroScheduleNo(i.getSroScheduleNo());
            item.setFedPayable(i.getFedPayable());
            item.setDiscount(i.getDiscount());
            item.setSaleType(i.getSaleType());
            item.setSroItemSerialNo(i.getSroItemSerialNo());
            return item;
        }).collect(Collectors.toList());

        payload.setItems(items);
        return payload;
    }

    @SuppressWarnings("unchecked")
    private String extractStatusCode(Object fbrResponse) {
        try {
            if (fbrResponse instanceof Map) {
                Map<String, Object> root = (Map<String, Object>) fbrResponse;
                Map<String, Object> validation = (Map<String, Object>) root.get("validationResponse");
                if (validation != null) {
                    return String.valueOf(validation.get("statusCode"));
                }
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }

    private FbrInvoicePayload entityToPayload(FbrInvoiceRequest invoice) {
        FbrInvoicePayload payload = new FbrInvoicePayload();
        payload.setInvoiceType(invoice.getInvoiceType());
        payload.setInvoiceDate(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().toString() : null);
        payload.setSellerNTNCNIC(invoice.getSellerNTNCNIC());
        payload.setSellerBusinessName(invoice.getSellerBusinessName());
        payload.setSellerProvince(invoice.getSellerProvince());
        payload.setSellerAddress(invoice.getSellerAddress());
        payload.setBuyerNTNCNIC(invoice.getBuyerNTNCNIC());
        payload.setBuyerBusinessName(invoice.getBuyerBusinessName());
        payload.setBuyerProvince(invoice.getBuyerProvince());
        payload.setBuyerAddress(invoice.getBuyerAddress());
        payload.setBuyerRegistrationType(invoice.getBuyerRegistrationType());
        payload.setInvoiceRefNo(invoice.getInvoiceRefNo());
        payload.setScenarioId(invoice.getScenarioId());

        List<FbrItemPayload> items = invoice.getItems().stream().map(i -> {
            FbrItemPayload item = new FbrItemPayload();
            item.setHsCode(i.getHsCode());
            item.setProductDescription(i.getProductDescription());
            item.setRate(i.getRate());
            item.setUoM(i.getUoM());
            item.setQuantity(i.getQuantity());
            item.setTotalValues(i.getTotalValues());
            item.setValueSalesExcludingST(i.getValueSalesExcludingST());
            item.setFixedNotifiedValueOrRetailPrice(i.getFixedNotifiedValueOrRetailPrice());
            item.setSalesTaxApplicable(i.getSalesTaxApplicable());
            item.setSalesTaxWithheldAtSource(i.getSalesTaxWithheldAtSource());
            item.setExtraTax(i.getExtraTax());
            item.setFurtherTax(i.getFurtherTax());
            item.setSroScheduleNo(i.getSroScheduleNo());
            item.setFedPayable(i.getFedPayable());
            item.setDiscount(i.getDiscount());
            item.setSaleType(i.getSaleType());
            item.setSroItemSerialNo(i.getSroItemSerialNo());
            return item;
        }).collect(Collectors.toList());

        payload.setItems(items);
        return payload;
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { return obj.toString(); }
    }
}
