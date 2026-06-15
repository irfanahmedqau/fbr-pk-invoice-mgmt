import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { BuyerService } from '../../../core/services/buyer.service';
import { BuyerProfile } from '../../../core/models/buyer.model';

const PROVINCES = ['PUNJAB', 'SINDH', 'KPK', 'BALOCHISTAN', 'AJK', 'GILGIT BALTISTAN', 'ISLAMABAD'];

@Component({
  selector: 'app-create-buyer-dialog',
  templateUrl: './create-buyer-dialog.component.html',
  styleUrl: './create-buyer-dialog.component.scss'
})
export class CreateBuyerDialogComponent implements OnInit {

  form!: FormGroup;
  provinces = PROVINCES;
  saving = false;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private buyerService: BuyerService,
    private dialogRef: MatDialogRef<CreateBuyerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { ntnCnic: string }
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      ntnCnic:      [{ value: this.data.ntnCnic, disabled: true }],
      businessName: ['', Validators.required],
      province:     ['', Validators.required],
      address:      [''],
    });
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.errorMessage = null;

    const v = this.form.getRawValue();
    this.buyerService.create({
      ntnCnic:      v.ntnCnic,
      businessName: v.businessName,
      province:     v.province,
      address:      v.address ?? '',
    }).subscribe({
      next: (result: BuyerProfile) => {
        this.saving = false;
        this.dialogRef.close(result);
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err.error?.message || err.message || 'Save failed.';
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
