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
    'invoiceRefNo', 'invoiceDate', 'buyerBusinessName',
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

  canResubmit(invoice: InvoiceSummary): boolean {
    return invoice.status === 'FAILED';
  }

  editAndResubmit(invoice: InvoiceSummary): void {
    this.router.navigate(['/manual-entry', invoice.id]);
  }

  viewInvoice(invoice: InvoiceSummary): void {
    this.router.navigate(['/manual-entry', invoice.id], { queryParams: { mode: 'view' } });
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
}
