package com.fbr.invoice.upload.service;

import com.fbr.invoice.upload.dto.FbrInvoicePayload;
import com.fbr.invoice.upload.dto.FbrItemPayload;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses FBR invoice Excel files.
 * Expected column layout (0-indexed, row 1 = header):
 *  0  Invoice Type          12 Scenario Id
 *  1  Invoice Date          13 Items (separator, ignored)
 *  2  Seller NTN CNIC       14 HS Code
 *  3  Seller Business Name  15 Product Description
 *  4  Seller Province       16 Rate
 *  5  Seller Address        17 UOM
 *  6  Buyer NTN CNIC        18 Quantity
 *  7  Buyer Business Name   19 Total Values
 *  8  Buyer Province        20 Value Sales Excl ST
 *  9  Buyer Address         21 Fixed Notified Value
 * 10  Buyer Reg Type        22 Sales Tax Applicable
 * 11  Invoice Ref No        23 Sales Tax Withheld
 *                           24 Extra Tax
 *                           25 Further Tax
 *                           26 SRO Schedule No
 *                           27 Fed Payable
 *                           28 Discount
 *                           29 Sale Type
 *                           30 SRO Item Serial No
 */
@Service
public class ExcelParserService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParserService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<FbrInvoicePayload> parseExcel(MultipartFile file, int sheetIndex) throws IOException {
        log.info("Parsing Excel file: {}, sheetIndex: {}", file.getOriginalFilename(), sheetIndex);
        List<FbrInvoicePayload> invoices = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            log.info("Sheet '{}' opened — physical rows: {}", sheet.getSheetName(), sheet.getPhysicalNumberOfRows());
            boolean firstRow = true;
            int rowNum = 1;

            for (Row row : sheet) {
                if (firstRow) { firstRow = false; continue; }
                if (isRowEmpty(row)) {
                    log.debug("Row {} is empty — skipping", rowNum);
                    rowNum++;
                    continue;
                }
                log.debug("Parsing row {}", rowNum);
                invoices.add(mapRow(row));
                rowNum++;
            }
        }
        log.info("Excel parsing complete — {} invoice rows extracted", invoices.size());
        return invoices;
    }

    public List<String> getSheetNames(MultipartFile file) throws IOException {
        log.info("Reading sheet names from: {}", file.getOriginalFilename());
        List<String> names = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                names.add(workbook.getSheetName(i));
            }
        }
        log.info("Found {} sheet(s): {}", names.size(), names);
        return names;
    }

    // ------------------------------------------------------------------

    private FbrInvoicePayload mapRow(Row row) {
        FbrInvoicePayload invoice = new FbrInvoicePayload();
        invoice.setInvoiceType(getString(row, 0));
        invoice.setInvoiceDate(getDateString(row, 1));
        invoice.setSellerNTNCNIC(getString(row, 2));
        invoice.setSellerBusinessName(getString(row, 3));
        invoice.setSellerProvince(getString(row, 4).trim());
        invoice.setSellerAddress(getString(row, 5));
        invoice.setBuyerNTNCNIC(getString(row, 6));
        invoice.setBuyerBusinessName(getString(row, 7));
        invoice.setBuyerProvince(getString(row, 8).trim());
        invoice.setBuyerAddress(getString(row, 9));
        invoice.setBuyerRegistrationType(getString(row, 10));
        invoice.setInvoiceRefNo(getString(row, 11).trim());
        invoice.setScenarioId(getString(row, 12));

        FbrItemPayload item = new FbrItemPayload();
        item.setHsCode(getString(row, 14));
        item.setProductDescription(getString(row, 15));
        item.setRate(formatRate(row, 16));
        item.setUoM(getString(row, 17));
        item.setQuantity(getDouble(row, 18));
        item.setTotalValues(getDouble(row, 19));
        item.setValueSalesExcludingST(getDouble(row, 20));
        item.setFixedNotifiedValueOrRetailPrice(getDouble(row, 21));
        item.setSalesTaxApplicable(getDouble(row, 22));
        item.setSalesTaxWithheldAtSource(getDouble(row, 23));
        item.setExtraTax(getString(row, 24));
        item.setFurtherTax(getDouble(row, 25));
        item.setSroScheduleNo(getString(row, 26));
        item.setFedPayable(getDouble(row, 27));
        item.setDiscount(getDouble(row, 28));
        item.setSaleType(getString(row, 29));
        item.setSroItemSerialNo(getString(row, 30));

        invoice.setItems(List.of(item));
        return invoice;
    }

    /**
     * Converts rate cell to FBR string format.
     * Excel decimal 0.18 → "18%", string "Exempt" → "Exempt", 0 → "0%"
     */
    private String formatRate(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "0%";

        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == 0) return "0%";
            long pct = Math.round(val * 100);
            return pct + "%";
        }
        if (cell.getCellType() == CellType.STRING) {
            String s = cell.getStringCellValue().trim();
            return s.isEmpty() ? "0%" : s;
        }
        return "0%";
    }

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: {
                double val = cell.getNumericCellValue();
                long lval = (long) val;
                return lval == val ? String.valueOf(lval) : String.valueOf(val);
            }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default:      return "";
        }
    }

    private String getDateString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
            return date.format(DATE_FMT);
        }
        return getString(row, col);
    }

    private double getDouble(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue().trim()); }
            catch (NumberFormatException e) { return 0.0; }
        }
        return 0.0;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
