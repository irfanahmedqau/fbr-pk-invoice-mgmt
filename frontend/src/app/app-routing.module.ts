import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'manual-entry',
    loadChildren: () =>
      import('./features/manual-entry/manual-entry.module').then(m => m.ManualEntryModule)
  },
  {
    path: 'excel-upload',
    loadChildren: () =>
      import('./features/excel-upload/excel-upload.module').then(m => m.ExcelUploadModule)
  },
  {
    path: 'invoice-status',
    loadChildren: () =>
      import('./features/invoice-status/invoice-status.module').then(m => m.InvoiceStatusModule)
  },
  { path: '', redirectTo: 'manual-entry', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
