import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { InvoiceService } from '../../../core/services/invoice.service';
import { InvoiceSummary, InvoiceStatus } from '../../../core/models/invoice.model';

@Component({
  selector: 'app-invoices-grid',
  templateUrl: './invoices-grid.component.html',
  styleUrl: './invoices-grid.component.scss'
})
export class InvoicesGridComponent implements OnInit {

  invoices: InvoiceSummary[] = [];
  loading = false;

  displayedColumns = [
    'invoiceRefNo', 'fbrInvRefNumber', 'invoiceDate', 'buyerBusinessName',
    'buyerNTNCNIC', 'scenarioId', 'processedAt', 'status', 'actions'
  ];

  constructor(
    private invoiceService: InvoiceService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.loading = true;
    this.invoiceService.getAllInvoices().subscribe({
      next: (data) => { this.invoices = data; this.loading = false; },
      error: ()    => { this.loading = false;
        this.snack.open('Could not load invoices.', 'Close', { duration: 4000 }); }
    });
  }

  openInvoice(invoice: InvoiceSummary): void {
    if (invoice.status === 'POSTED' || invoice.status === 'VALIDATED') {
      this.router.navigate(['/manual-entry', invoice.id], { queryParams: { mode: 'view' } });
    } else if (invoice.status === 'FAILED') {
      this.router.navigate(['/manual-entry', invoice.id], { queryParams: { mode: 'edit' } });
    } else {
      this.router.navigate(['/manual-entry', invoice.id], { queryParams: { mode: 'submit' } });
    }
  }

  fbrErrorTooltip(invoice: InvoiceSummary): string {
    if (invoice.status !== 'FAILED' || !invoice.validationResponse) return '';
    try {
      const r = JSON.parse(invoice.validationResponse);
      const vr = r?.validationResponse;
      if (!vr) return invoice.validationResponse;

      const lines: string[] = [];
      if (vr.error) lines.push(vr.error);
      for (const s of vr.invoiceStatuses ?? []) {
        if (s.error) lines.push(`Item ${s.itemSNo} [${s.errorCode ?? s.statusCode}]: ${s.error}`);
      }
      return lines.length ? lines.join('\n') : `Status ${vr.statusCode}: ${vr.status}`;
    } catch {
      return invoice.validationResponse;
    }
  }

  statusClass(status: InvoiceStatus): string {
    const map: Record<InvoiceStatus, string> = {
      PENDING:   'chip-pending',
      VALIDATED: 'chip-validated',
      POSTED:    'chip-posted',
      FAILED:    'chip-failed'
    };
    return map[status] ?? '';
  }

  statusIcon(status: InvoiceStatus): string {
    const map: Record<InvoiceStatus, string> = {
      PENDING:   'hourglass_empty',
      VALIDATED: 'verified',
      POSTED:    'check_circle',
      FAILED:    'cancel'
    };
    return map[status] ?? 'help';
  }

  downloadPdf(invoice: InvoiceSummary): void {
    this.invoiceService.downloadInvoicePdf(invoice.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `invoice-${invoice.invoiceRefNo}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.snack.open('Could not download PDF.', 'Close', { duration: 4000 })
    });
  }
}
