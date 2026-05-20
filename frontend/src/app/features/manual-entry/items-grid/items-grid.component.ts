import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormArray } from '@angular/forms';

@Component({
  selector: 'app-items-grid',
  templateUrl: './items-grid.component.html',
  styleUrl: './items-grid.component.scss'
})
export class ItemsGridComponent {
  @Input() items!: FormArray;
  @Output() removeItem = new EventEmitter<number>();

  displayedColumns = [
    'hsCode', 'productDescription', 'rate', 'uoM', 'quantity',
    'totalValues', 'salesTaxApplicable', 'saleType', 'actions'
  ];

  get dataSource() {
    return this.items.controls.map((c, i) => ({ ...c.value, _index: i }));
  }

  onRemove(index: number): void {
    this.removeItem.emit(index);
  }
}
