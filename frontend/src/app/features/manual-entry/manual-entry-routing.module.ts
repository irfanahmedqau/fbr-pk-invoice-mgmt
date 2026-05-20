import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ManualEntryComponent } from './manual-entry/manual-entry.component';

const routes: Routes = [
  { path: '',    component: ManualEntryComponent },
  { path: ':id', component: ManualEntryComponent }   // edit / view mode
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ManualEntryRoutingModule { }
