package com.fbr.invoice.upload.dto;

public class ExcelRowResult {

    private int rowNumber;
    private String invoiceRefNo;
    private String status;   // "SUCCESS" | "FAILED"
    private String message;
    private Object fbrResponse;

    public ExcelRowResult() {}

    public ExcelRowResult(int rowNumber, String invoiceRefNo, String status, String message, Object fbrResponse) {
        this.rowNumber = rowNumber;
        this.invoiceRefNo = invoiceRefNo;
        this.status = status;
        this.message = message;
        this.fbrResponse = fbrResponse;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getInvoiceRefNo() { return invoiceRefNo; }
    public void setInvoiceRefNo(String invoiceRefNo) { this.invoiceRefNo = invoiceRefNo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getFbrResponse() { return fbrResponse; }
    public void setFbrResponse(Object fbrResponse) { this.fbrResponse = fbrResponse; }
}
