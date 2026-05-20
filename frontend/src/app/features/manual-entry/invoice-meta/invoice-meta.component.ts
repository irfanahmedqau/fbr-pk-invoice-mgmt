import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';

const SCENARIOS = [
  { id: 'SN001', label: 'SN001 - Goods at Standard Rate (Default)' },
  { id: 'SN002', label: 'SN002 - Goods to Unregistered Buyers' },
  { id: 'SN005', label: 'SN005 - Goods at Reduced Rate' },
  { id: 'SN006', label: 'SN006 - Exempt Goods' },
  { id: 'SN007', label: 'SN007 - Zero-Rated Goods' },
  { id: 'SN009', label: 'SN009 - Cotton Ginners' },
  { id: 'SN016', label: 'SN016 - Processing / Conversion of Goods' },
  { id: 'SN017', label: 'SN017 - Goods (FED in ST Mode)' },
  { id: 'SN024', label: 'SN024 - Goods as per SRO.297(I)/2023' },
];

@Component({
  selector: 'app-invoice-meta',
  templateUrl: './invoice-meta.component.html',
  styleUrl: './invoice-meta.component.scss'
})
export class InvoiceMetaComponent implements OnInit {
  @Input() parentForm!: FormGroup;
  scenarios = SCENARIOS;
  maxDate = new Date();

  ngOnInit(): void {
    // Default invoice date to today
    if (!this.parentForm.get('invoiceDate')?.value) {
      this.parentForm.get('invoiceDate')?.setValue(new Date());
    }
  }
}
