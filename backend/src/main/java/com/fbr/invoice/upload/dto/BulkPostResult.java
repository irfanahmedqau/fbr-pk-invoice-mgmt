package com.fbr.invoice.upload.dto;

import java.util.List;

public class BulkPostResult {

    private int totalValidated;
    private int postedCount;
    private int failedCount;
    private List<ExcelRowResult> results;

    public BulkPostResult(int totalValidated, int postedCount, int failedCount, List<ExcelRowResult> results) {
        this.totalValidated = totalValidated;
        this.postedCount = postedCount;
        this.failedCount = failedCount;
        this.results = results;
    }

    public int getTotalValidated() { return totalValidated; }
    public int getPostedCount() { return postedCount; }
    public int getFailedCount() { return failedCount; }
    public List<ExcelRowResult> getResults() { return results; }
}
