import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { ExcelUploadRoutingModule } from './excel-upload-routing.module';
import { ExcelUploadComponent } from './excel-upload/excel-upload.component';

@NgModule({
  declarations: [ExcelUploadComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ExcelUploadRoutingModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatTableModule,
    MatProgressBarModule,
    MatChipsModule,
    MatDividerModule,
    MatSnackBarModule,
  ]
})
export class ExcelUploadModule { }
