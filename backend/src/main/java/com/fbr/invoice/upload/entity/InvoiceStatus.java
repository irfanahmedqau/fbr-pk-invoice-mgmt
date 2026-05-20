package com.fbr.invoice.upload.entity;

public enum InvoiceStatus {
    PENDING,     // saved to DB, not yet sent to FBR
    VALIDATED,   // FBR validation passed (statusCode: "00")
    POSTED,      // successfully channeled to FBR (step 2)
    FAILED       // validation or channeling failed — eligible for resubmit
}
