import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InvoicesGridComponent } from './invoices-grid/invoices-grid.component';

const routes: Routes = [{ path: '', component: InvoicesGridComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvoiceStatusRoutingModule { }
