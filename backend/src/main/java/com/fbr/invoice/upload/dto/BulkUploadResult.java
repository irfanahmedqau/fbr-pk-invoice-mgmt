package com.fbr.invoice.upload.dto;

import java.util.List;

public class BulkUploadResult {

    private int totalRows;
    private int successCount;
    private int failedCount;
    private int skippedCount;   // already POSTED — duplicate prevention
    private List<ExcelRowResult> results;

    public BulkUploadResult() {}

    public BulkUploadResult(int totalRows, int successCount, int failedCount,
                            int skippedCount, List<ExcelRowResult> results) {
        this.totalRows    = totalRows;
        this.successCount = successCount;
        this.failedCount  = failedCount;
        this.skippedCount = skippedCount;
        this.results      = results;
    }

    public int getTotalRows()    { return totalRows; }
    public int getSuccessCount() { return successCount; }
    public int getFailedCount()  { return failedCount; }
    public int getSkippedCount() { return skippedCount; }
    public List<ExcelRowResult> getResults() { return results; }

    public void setTotalRows(int totalRows)       { this.totalRows = totalRows; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public void setFailedCount(int failedCount)   { this.failedCount = failedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
    public void setResults(List<ExcelRowResult> results) { this.results = results; }
}
