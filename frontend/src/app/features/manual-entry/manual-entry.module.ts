import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDialogModule } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatRadioModule } from '@angular/material/radio';

import { ManualEntryRoutingModule } from './manual-entry-routing.module';
import { ManualEntryComponent } from './manual-entry/manual-entry.component';
import { SellerFormComponent } from './seller-form/seller-form.component';
import { BuyerFormComponent } from './buyer-form/buyer-form.component';
import { InvoiceMetaComponent } from './invoice-meta/invoice-meta.component';
import { ItemEntryFormComponent } from './item-entry-form/item-entry-form.component';
import { ItemsGridComponent } from './items-grid/items-grid.component';
import { CreateBuyerDialogComponent } from './create-buyer-dialog/create-buyer-dialog.component';

@NgModule({
  declarations: [
    ManualEntryComponent,
    SellerFormComponent,
    BuyerFormComponent,
    InvoiceMetaComponent,
    ItemEntryFormComponent,
    ItemsGridComponent,
    CreateBuyerDialogComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    ManualEntryRoutingModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDividerModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatDialogModule,
    MatListModule,
    MatRadioModule,
  ]
})
export class ManualEntryModule { }
