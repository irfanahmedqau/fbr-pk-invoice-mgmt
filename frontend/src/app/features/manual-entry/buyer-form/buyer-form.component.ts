import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';
import { FbrLookupService } from '../../../core/services/fbr-lookup.service';

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

  constructor(private fbrLookup: FbrLookupService) {}

  ngOnInit(): void {
    // Registration type is always determined by FBR API — never allow manual editing
    this.parentForm.get('buyerRegistrationType')?.disable();

    this.parentForm.get('buyerNTNCNIC')?.valueChanges.pipe(
      debounceTime(900),
      distinctUntilChanged(),
      filter(v => v && String(v).trim().length >= 5)
    ).subscribe(ntn => this.runValidation(String(ntn).trim()));

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
      next: (res) => {
        this.validatingRegType = false;
        const raw = res?.['REGISTRATION_TYPE'] || res?.['registrationType'] || null;
        // Normalize casing: "registered" → "Registered", "unregistered" → "Unregistered"
        // Fall back to Registered: this is only called when ATL is active, so buyer is registered.
        const regType = raw ? this.normalizeRegType(raw) : 'Registered';
        this.setRegType(regType);
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

  private normalizeRegType(value: string): string {
    const lower = value.toLowerCase().trim();
    if (lower === 'registered')   return 'Registered';
    if (lower === 'unregistered') return 'Unregistered';
    // Return as-is with first letter capitalised for anything else
    return value.charAt(0).toUpperCase() + value.slice(1);
  }

  private resetVerification(): void {
    this.atlResult     = null;
    this.regTypeResult = null;
    this.atlError      = null;
    this.parentForm.get('buyerRegistrationType')?.setValue('');
  }

  private formatDate(value: any): string {
    if (!value) return new Date().toISOString().split('T')[0];
    if (value instanceof Date) return value.toISOString().split('T')[0];
    return String(value);
  }
}
