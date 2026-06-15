import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { FbrLookupService } from '../../../core/services/fbr-lookup.service';
import { BuyerService } from '../../../core/services/buyer.service';
import { BuyerBusiness, BuyerProfile } from '../../../core/models/buyer.model';
import { CreateBuyerDialogComponent } from '../create-buyer-dialog/create-buyer-dialog.component';

const PROVINCES = ['PUNJAB', 'SINDH', 'KPK', 'BALOCHISTAN', 'AJK', 'GILGIT BALTISTAN', 'ISLAMABAD'];

export interface AtlResult {
  isActive: boolean;
  label: string;
}

export interface RegTypeResult {
  registrationType: string;
  label: string;
}

@Component({
  selector: 'app-buyer-form',
  templateUrl: './buyer-form.component.html',
  styleUrl: './buyer-form.component.scss'
})
export class BuyerFormComponent implements OnInit {

  @Input() parentForm!: FormGroup;

  provinces = PROVINCES;

  validatingAtl     = false;
  validatingRegType = false;

  atlResult:     AtlResult     | null = null;
  regTypeResult: RegTypeResult | null = null;
  atlError:      string        | null = null;

  buyerBusinessOptions: BuyerBusiness[] = [];
  selectedBusiness:     BuyerBusiness | null = null;
  showCreateBuyerPrompt = false;
  creatingBuyer = false;

  constructor(
    private fbrLookup: FbrLookupService,
    private buyerService: BuyerService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    // Registration type is always determined by FBR API — never allow manual editing
    this.parentForm.get('buyerRegistrationType')?.disable();

    this.parentForm.get('buyerNTNCNIC')?.valueChanges.pipe(
      debounceTime(900),
      distinctUntilChanged(),
      filter(v => v && String(v).trim().length >= 5)
    ).subscribe(ntn => this.runValidation(String(ntn).trim()));

    // Directory lookup — separate subscription so FBR validation is unaffected
    this.parentForm.get('buyerNTNCNIC')?.valueChanges.pipe(
      debounceTime(900),
      distinctUntilChanged(),
      filter(v => v && String(v).trim().length >= 5)
    ).subscribe(ntn => this.lookupDirectory(String(ntn).trim()));

    // Reset reg type if NTN is cleared
    this.parentForm.get('buyerNTNCNIC')?.valueChanges.pipe(
      filter(v => !v || String(v).trim().length < 5)
    ).subscribe(() => this.resetVerification());
  }

  runValidation(ntn: string): void {
    this.atlResult     = null;
    this.regTypeResult = null;
    this.atlError      = null;
    this.validatingAtl = true;
    this.parentForm.get('buyerRegistrationType')?.setValue('');

    const invoiceDate = this.parentForm.get('invoiceDate')?.value;
    const date = this.formatDate(invoiceDate);

    // Step 1 — ATL check
    this.fbrLookup.getBuyerAtl(ntn, date).subscribe({
      next: (res) => {
        this.validatingAtl = false;
        const isActive = res?.['status'] === 'Active';

        this.atlResult = {
          isActive,
          label: isActive
            ? `Active on Sales Tax ATL (code: ${res?.['status code'] ?? res?.['statusCode'] ?? '—'})`
            : `NOT Active on ATL — status: "${res?.['status'] ?? 'unknown'}"`
        };

        if (isActive) {
          this.fetchRegType(ntn);
        }
      },
      error: (err) => {
        this.validatingAtl = false;
        this.atlError = 'ATL check failed: ' + (err.error?.message || err.message || 'Network error');
      }
    });
  }

  private fetchRegType(ntn: string): void {
    this.validatingRegType = true;

    this.fbrLookup.getBuyerRegType(ntn).subscribe({
      next: (_res) => {
        this.validatingRegType = false;
        // ATL is active → always Registered regardless of reg-type API response.
        this.setRegType('Registered');
      },
      error: () => {
        // ATL is active (fetchRegType is only called when isActive=true), so treat API errors
        // as Registered rather than Unregistered — the ATL status is the authoritative signal.
        this.validatingRegType = false;
        this.setRegType('Registered');
      }
    });
  }

  private setRegType(regType: string): void {
    this.regTypeResult = { registrationType: regType, label: regType };
    // enable → setValue → disable forces Angular Material to re-render the disabled input.
    const ctrl = this.parentForm.get('buyerRegistrationType');
    ctrl?.enable({ emitEvent: false });
    ctrl?.setValue(regType);
    ctrl?.disable({ emitEvent: false });
  }

  private lookupDirectory(ntn: string): void {
    this.showCreateBuyerPrompt = false;
    this.buyerBusinessOptions  = [];
    this.selectedBusiness      = null;

    this.buyerService.getByNtnCnic(ntn).subscribe({
      next: (profile) => {
        if (profile.businesses.length === 1) {
          this.autofillBusiness(profile.businesses[0]);
        } else if (profile.businesses.length > 1) {
          this.buyerBusinessOptions = profile.businesses;
        }
      },
      error: (err) => {
        if (err.status === 404) {
          this.showCreateBuyerPrompt = true;
        }
        // silently ignore all other errors — do not disturb FBR flow
      }
    });
  }

  autofillBusiness(b: BuyerBusiness): void {
    this.parentForm.patchValue({
      buyerBusinessName: b.businessName,
      buyerProvince:     b.province,
      buyerAddress:      b.address,
    });
    this.selectedBusiness     = b;
    this.buyerBusinessOptions = [];
  }

  openCreateBuyerDialog(): void {
    const ntn = String(this.parentForm.get('buyerNTNCNIC')?.value ?? '').trim();
    const ref = this.dialog.open(CreateBuyerDialogComponent, {
      width: '480px',
      data: { ntnCnic: ntn },
    });
    ref.afterClosed().subscribe((result: BuyerProfile | undefined) => {
      if (result) {
        const def = result.businesses.find(b => b.isDefault) ?? result.businesses[0];
        if (def) this.autofillBusiness(def);
        this.showCreateBuyerPrompt = false;
      }
    });
  }

  private resetVerification(): void {
    this.atlResult             = null;
    this.regTypeResult         = null;
    this.atlError              = null;
    this.showCreateBuyerPrompt = false;
    this.buyerBusinessOptions  = [];
    this.selectedBusiness      = null;
    this.parentForm.get('buyerRegistrationType')?.setValue('');
  }

  private formatDate(value: any): string {
    if (!value) return new Date().toISOString().split('T')[0];
    if (value instanceof Date) return value.toISOString().split('T')[0];
    return String(value);
  }
}
