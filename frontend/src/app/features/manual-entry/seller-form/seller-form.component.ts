import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';

const PROVINCES = ['PUNJAB', 'SINDH', 'KPK', 'BALOCHISTAN', 'AJK', 'GILGIT BALTISTAN', 'ISLAMABAD'];

@Component({
  selector: 'app-seller-form',
  templateUrl: './seller-form.component.html',
  styleUrl: './seller-form.component.scss'
})
export class SellerFormComponent implements OnInit {
  @Input() parentForm!: FormGroup;
  provinces = PROVINCES;

  ngOnInit(): void {}
}
