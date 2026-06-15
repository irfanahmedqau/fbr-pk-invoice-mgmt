import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormArray } from '@angular/forms';
import { FbrItem } from '../../../core/models/invoice.model';

@Component({
  selector: 'app-items-grid',
  templateUrl: './items-grid.component.html',
  styleUrl: './items-grid.component.scss'
})
export class ItemsGridComponent {
  @Input() items!: FormArray;
  @Input() viewOnly = false;
  @Output() removeItem = new EventEmitter<number>();
  @Output() editItem  = new EventEmitter<{ item: FbrItem; index: number }>();

  displayedColumns = [
    'hsCode', 'productDescription', 'rate', 'uoM', 'quantity',
    'totalValues', 'salesTaxApplicable', 'saleType', 'actions'
  ];

  get dataSource() {
    return this.items.controls.map((c, i) => ({ ...c.getRawValue(), _index: i }));
  }

  onEdit(index: number): void {
    this.editItem.emit({ item: this.items.at(index).getRawValue() as FbrItem, index });
  }

  onRemove(index: number): void {
    this.removeItem.emit(index);
  }
}
