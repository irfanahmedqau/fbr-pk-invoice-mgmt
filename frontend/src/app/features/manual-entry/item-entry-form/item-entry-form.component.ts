import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { FbrItem, HsCode } from '../../../core/models/invoice.model';
import { FbrLookupService } from '../../../core/services/fbr-lookup.service';

const UOM_OPTIONS  = ['KG', 'L', 'M', 'Unit', 'MT', 'Dozen', 'Pair', 'Set'];
const SALE_TYPES   = [
  'Goods at standard rate (default)',
  'Goods at Standard Rate to Unregistered Buyers',
  'Goods at Reduced Rate',
  'Exempt goods',
  'Goods at zero-rate',
  'Cotton ginners',
  'Processing/Conversion of Goods',
  'Goods (FED in ST Mode)',
  'Goods as per SRO.297(|)/2023',
];

@Component({
  selector: 'app-item-entry-form',
  templateUrl: './item-entry-form.component.html',
  styleUrl: './item-entry-form.component.scss'
})
export class ItemEntryFormComponent implements OnInit, OnDestroy {

  @Output() itemAdded   = new EventEmitter<FbrItem>();
  @Output() itemUpdated = new EventEmitter<{ item: FbrItem; index: number }>();

  private _editIndex: number | null = null;

  get editIndex(): number | null { return this._editIndex; }

  @Input() set editRequest(req: { item: FbrItem; index: number } | null) {
    this._editIndex = req?.index ?? null;
    if (req && this.itemForm) {
      this.itemForm.patchValue(req.item);
    }
  }

  itemForm!: FormGroup;
  uomOptions = UOM_OPTIONS;
  saleTypes  = SALE_TYPES;

  allHsCodes:      HsCode[] = [];
  filteredHsCodes: HsCode[] = [];
  hsCodesLoading = false;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private fbrLookup: FbrLookupService
  ) {}

  ngOnInit(): void {
    this.itemForm = this.fb.group({
      hsCode:                          ['', Validators.required],
      productDescription:              [''],
      rate:                            ['18%', Validators.required],
      uoM:                             ['KG', Validators.required],
      quantity:                        [0, [Validators.required, Validators.min(0.001)]],
      totalValues:                     [{ value: 0, disabled: true }],
      valueSalesExcludingST:           [0],
      fixedNotifiedValueOrRetailPrice: [0],
      salesTaxApplicable:              [0],
      salesTaxWithheldAtSource:        [0],
      extraTax:                        [''],
      furtherTax:                      [0],
      sroScheduleNo:                   [''],
      fedPayable:                      [0],
      discount:                        [0],
      saleType:                        ['Goods at standard rate (default)', Validators.required],
      sroItemSerialNo:                 [''],
    });

    this.loadHsCodes();

    // Filter as user types in HS Code field
    this.itemForm.get('hsCode')?.valueChanges.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(val => this.filterHsCodes(val));

    // Recalculate GST and total whenever value-excl-ST or rate changes
    this.itemForm.get('valueSalesExcludingST')?.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => this.recalcFinancials());

    this.itemForm.get('rate')?.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => this.recalcFinancials());

    // When user manually edits GST, keep total in sync
    this.itemForm.get('salesTaxApplicable')?.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => this.recalcTotal());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private parseRatePercent(rateStr: string): number {
    if (!rateStr) return 0;
    const match = rateStr.match(/[\d.]+/);
    return match ? parseFloat(match[0]) / 100 : 0;
  }

  private recalcFinancials(): void {
    const valueExclST = parseFloat(this.itemForm.get('valueSalesExcludingST')?.value) || 0;
    const rateStr     = this.itemForm.get('rate')?.value ?? '';
    const rateDecimal = this.parseRatePercent(rateStr);

    const gst = parseFloat((valueExclST * rateDecimal).toFixed(2));
    this.itemForm.get('salesTaxApplicable')?.setValue(gst, { emitEvent: false });
    this.recalcTotal();
  }

  private recalcTotal(): void {
    const valueExclST = parseFloat(this.itemForm.get('valueSalesExcludingST')?.value) || 0;
    const gst         = parseFloat(this.itemForm.get('salesTaxApplicable')?.value) || 0;
    const total       = parseFloat((valueExclST + gst).toFixed(2));
    this.itemForm.get('totalValues')?.setValue(total, { emitEvent: false });
  }

  private loadHsCodes(): void {
    this.hsCodesLoading = true;
    this.fbrLookup.getHsCodes().subscribe({
      next: (codes) => {
        this.allHsCodes  = codes;
        this.hsCodesLoading = false;
      },
      error: () => { this.hsCodesLoading = false; }
    });
  }

  filterHsCodes(value: string): void {
    if (!value || value.length < 2) {
      this.filteredHsCodes = [];
      return;
    }
    const lower = value.toLowerCase();
    this.filteredHsCodes = this.allHsCodes
      .filter(c =>
        c.hS_CODE.toLowerCase().includes(lower) ||
        c.description.toLowerCase().includes(lower)
      )
      .slice(0, 50);  // cap at 50 to keep the dropdown fast
  }

  onHsCodeSelected(event: MatAutocompleteSelectedEvent): void {
    const code = event.option.value as HsCode;
    this.itemForm.get('hsCode')?.setValue(code.hS_CODE, { emitEvent: false });
    this.itemForm.get('productDescription')?.setValue(code.description);
    this.filteredHsCodes = [];
  }

  /** Display function for mat-autocomplete — show only the code in the input after selection. */
  displayHsCode(code: HsCode | string): string {
    if (!code) return '';
    return typeof code === 'string' ? code : code.hS_CODE;
  }

  saveItem(): void {
    if (this.itemForm.invalid) { this.itemForm.markAllAsTouched(); return; }

    const raw = this.itemForm.getRawValue() as FbrItem;

    if (this._editIndex !== null) {
      this.itemUpdated.emit({ item: raw, index: this._editIndex });
    } else {
      this.itemAdded.emit(raw);
    }

    this.resetForm();
  }

  cancelEdit(): void {
    this._editIndex = null;
    this.resetForm();
  }

  private resetForm(): void {
    this._editIndex = null;
    this.itemForm.reset({
      rate: '18%', uoM: 'KG', quantity: 0, totalValues: 0,
      valueSalesExcludingST: 0, fixedNotifiedValueOrRetailPrice: 0,
      salesTaxApplicable: 0, salesTaxWithheldAtSource: 0,
      extraTax: '', furtherTax: 0, sroScheduleNo: '', fedPayable: 0,
      discount: 0, saleType: 'Goods at standard rate (default)', sroItemSerialNo: ''
    });
    this.filteredHsCodes = [];
  }
}
