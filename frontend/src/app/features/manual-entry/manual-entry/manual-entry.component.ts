import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { InvoiceService } from '../../../core/services/invoice.service';
import { FbrInvoicePayload, FbrItem, InvoiceDetail } from '../../../core/models/invoice.model';

export type EntryMode = 'new' | 'edit' | 'view';

@Component({
  selector: 'app-manual-entry',
  templateUrl: './manual-entry.component.html',
  styleUrl: './manual-entry.component.scss'
})
export class ManualEntryComponent implements OnInit {

  invoiceForm!: FormGroup;
  mode: EntryMode = 'new';
  invoiceId: number | null = null;
  saving = false;
  uploading = false;
  uploadDone = false;
  loadingInvoice = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private invoiceService: InvoiceService,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.buildForm();

    const id = this.route.snapshot.paramMap.get('id');
    const viewMode = this.route.snapshot.queryParamMap.get('mode');

    if (id) {
      this.invoiceId = +id;
      this.mode = viewMode === 'view' ? 'view' : 'edit';
      this.loadInvoice(this.invoiceId);
    }
  }

  get isViewOnly(): boolean { return this.mode === 'view'; }
  get isEditMode(): boolean { return this.mode === 'edit'; }
  get isNewMode():  boolean { return this.mode === 'new'; }

  get items(): FormArray {
    return this.invoiceForm.get('items') as FormArray;
  }

  addItem(item: FbrItem): void {
    if (this.isViewOnly) return;
    this.items.push(this.fb.group({
      hsCode:                         [item.hsCode, Validators.required],
      productDescription:             [item.productDescription],
      rate:                           [item.rate, Validators.required],
      uoM:                            [item.uoM, Validators.required],
      quantity:                       [item.quantity, [Validators.required, Validators.min(0.001)]],
      totalValues:                    [item.totalValues, Validators.required],
      valueSalesExcludingST:          [item.valueSalesExcludingST],
      fixedNotifiedValueOrRetailPrice:[item.fixedNotifiedValueOrRetailPrice],
      salesTaxApplicable:             [item.salesTaxApplicable],
      salesTaxWithheldAtSource:       [item.salesTaxWithheldAtSource],
      extraTax:                       [item.extraTax],
      furtherTax:                     [item.furtherTax],
      sroScheduleNo:                  [item.sroScheduleNo],
      fedPayable:                     [item.fedPayable],
      discount:                       [item.discount],
      saleType:                       [item.saleType, Validators.required],
      sroItemSerialNo:                [item.sroItemSerialNo],
    }));
  }

  removeItem(index: number): void {
    if (this.isViewOnly) return;
    this.items.removeAt(index);
  }

  saveToDb(): void {
    if (this.invoiceForm.invalid) { this.invoiceForm.markAllAsTouched(); return; }
    this.saving = true;
    this.invoiceService.saveInvoice(this.buildPayload()).subscribe({
      next: () => {
        this.saving = false;
        this.snack.open('Invoice saved to database.', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.saving = false;
        this.snack.open('Save failed: ' + (err.error?.message || err.message), 'Close', { duration: 5000 });
      }
    });
  }

  submitToFbr(): void {
    if (this.invoiceForm.invalid) { this.invoiceForm.markAllAsTouched(); return; }
    if (this.items.length === 0) {
      this.snack.open('Add at least one invoice item before submitting.', 'Close', { duration: 4000 });
      return;
    }
    this.uploading = true;
    this.invoiceService.uploadToFbr(this.buildPayload()).subscribe({
      next: (res) => {
        this.uploading = false;
        this.uploadDone = true;
        this.snack.open('Invoice submitted to FBR — VALIDATED!', 'OK', { duration: 4000 });
        console.log('FBR response:', res);
      },
      error: (err) => {
        this.uploading = false;
        const msg = err.error?.message || err.message || 'Unknown error';
        this.snack.open('FBR submission failed: ' + msg, 'Close', { duration: 6000 });
      }
    });
  }

  resubmitToFbr(): void {
    if (!this.invoiceId) return;
    if (this.invoiceForm.invalid) { this.invoiceForm.markAllAsTouched(); return; }
    if (this.items.length === 0) {
      this.snack.open('Add at least one invoice item before resubmitting.', 'Close', { duration: 4000 });
      return;
    }
    this.uploading = true;
    this.invoiceService.resubmitInvoice(this.invoiceId, this.buildPayload()).subscribe({
      next: (res) => {
        this.uploading = false;
        this.uploadDone = true;
        this.snack.open('Invoice resubmitted — VALIDATED!', 'OK', { duration: 4000 });
        console.log('FBR resubmit response:', res);
      },
      error: (err) => {
        this.uploading = false;
        const msg = err.error?.message || err.message || 'Unknown error';
        this.snack.open('Resubmit failed: ' + msg, 'Close', { duration: 6000 });
      }
    });
  }

  backToGrid(): void {
    this.router.navigate(['/invoice-status']);
  }

  resetForm(): void {
    this.buildForm();
    this.uploadDone = false;
    this.mode = 'new';
    this.invoiceId = null;
  }

  // ------------------------------------------------------------------

  private buildForm(): void {
    this.invoiceForm = this.fb.group({
      invoiceType:           ['Sale Invoice', Validators.required],
      invoiceDate:           ['', Validators.required],
      sellerNTNCNIC:         ['2701129', Validators.required],
      sellerBusinessName:    ['BASFA TEXTILE PVT LTD', Validators.required],
      sellerProvince:        ['PUNJAB', Validators.required],
      sellerAddress:         ['AL-NOOR TOWN, 20 KM FEROZEPUR ROAD LAHORE', Validators.required],
      buyerNTNCNIC:          ['', Validators.required],
      buyerBusinessName:     ['', Validators.required],
      buyerProvince:         ['', Validators.required],
      buyerAddress:          ['', Validators.required],
      buyerRegistrationType: ['Registered', Validators.required],
      invoiceRefNo:          ['', Validators.required],
      scenarioId:            ['SN001', Validators.required],
      items:                 this.fb.array([], Validators.minLength(1))
    });
  }

  private loadInvoice(id: number): void {
    this.loadingInvoice = true;
    this.invoiceService.getInvoiceById(id).subscribe({
      next: (invoice: InvoiceDetail) => {
        this.loadingInvoice = false;
        this.patchForm(invoice);
        if (this.isViewOnly) this.invoiceForm.disable();
      },
      error: () => {
        this.loadingInvoice = false;
        this.snack.open('Could not load invoice.', 'Close', { duration: 4000 });
      }
    });
  }

  private patchForm(invoice: InvoiceDetail): void {
    this.invoiceForm.patchValue({
      invoiceType:           invoice.invoiceType,
      invoiceDate:           invoice.invoiceDate,
      sellerNTNCNIC:         invoice.sellerNTNCNIC,
      sellerBusinessName:    invoice.sellerBusinessName,
      sellerProvince:        invoice.sellerProvince,
      sellerAddress:         invoice.sellerAddress,
      buyerNTNCNIC:          invoice.buyerNTNCNIC,
      buyerBusinessName:     invoice.buyerBusinessName,
      buyerProvince:         invoice.buyerProvince,
      buyerAddress:          invoice.buyerAddress,
      buyerRegistrationType: invoice.buyerRegistrationType,
      invoiceRefNo:          invoice.invoiceRefNo,
      scenarioId:            invoice.scenarioId,
    });

    this.items.clear();
    (invoice.items || []).forEach(item => this.addItem(item));
  }

  private buildPayload(): FbrInvoicePayload {
    const v = this.invoiceForm.getRawValue();
    return {
      ...v,
      invoiceDate: v.invoiceDate instanceof Date
        ? v.invoiceDate.toISOString().split('T')[0]
        : v.invoiceDate,
      items: v.items as FbrItem[]
    };
  }
}
