import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { InvoiceService } from '../../../core/services/invoice.service';
import { FbrLookupService } from '../../../core/services/fbr-lookup.service';
import { BulkUploadResult, ExcelRowResult } from '../../../core/models/invoice.model';

@Component({
  selector: 'app-excel-upload',
  templateUrl: './excel-upload.component.html',
  styleUrl: './excel-upload.component.scss'
})
export class ExcelUploadComponent {

  selectedFile: File | null = null;
  sheetNames: string[] = [];
  selectedSheetIndex = 0;
  loadingSheets = false;
  uploading = false;

  result: BulkUploadResult | null = null;

  displayedColumns = ['rowNumber', 'invoiceRefNo', 'status', 'message'];

  constructor(
    private invoiceService: InvoiceService,
    private fbrLookup: FbrLookupService,
    private snack: MatSnackBar
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.selectedFile = file;
    this.sheetNames = [];
    this.selectedSheetIndex = 0;
    this.result = null;

    // Fetch sheet names so user can pick the correct sheet
    this.loadingSheets = true;
    this.fbrLookup.getExcelSheets(file).subscribe({
      next: (names) => {
        this.loadingSheets = false;
        this.sheetNames = names;
      },
      error: () => {
        this.loadingSheets = false;
        this.snack.open('Could not read Excel sheets. Ensure file is .xlsx format.', 'Close', { duration: 4000 });
      }
    });
  }

  upload(): void {
    if (!this.selectedFile) return;
    this.uploading = true;
    this.result = null;

    this.invoiceService.uploadExcel(this.selectedFile, this.selectedSheetIndex).subscribe({
      next: (res) => {
        this.uploading = false;
        this.result = res;
        const msg = `Done: ${res.successCount} submitted, ${res.failedCount} failed out of ${res.totalRows} rows.`;
        this.snack.open(msg, 'OK', { duration: 6000 });
      },
      error: (err) => {
        this.uploading = false;
        this.snack.open('Upload failed: ' + (err.error?.message || err.message), 'Close', { duration: 6000 });
      }
    });
  }

  reset(): void {
    this.selectedFile = null;
    this.sheetNames = [];
    this.selectedSheetIndex = 0;
    this.result = null;
  }

  statusClass(row: ExcelRowResult): string {
    return row.status === 'SUCCESS' ? 'status-success' : 'status-failed';
  }
}
