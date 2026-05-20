import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BulkUploadResult, FbrInvoicePayload, InvoiceDetail, InvoiceSummary } from '../models/invoice.model';

@Injectable({ providedIn: 'root' })
export class InvoiceService {

  private base = 'http://localhost:8081/api/invoices';

  constructor(private http: HttpClient) {}

  saveInvoice(payload: FbrInvoicePayload): Observable<any> {
    return this.http.post(`${this.base}/save-invoice`, payload);
  }

  uploadToFbr(payload: FbrInvoicePayload): Observable<any> {
    return this.http.post(`${this.base}/upload`, payload);
  }

  uploadExcel(file: File, sheetIndex: number): Observable<BulkUploadResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<BulkUploadResult>(
      `${this.base}/upload-excel?sheetIndex=${sheetIndex}`,
      formData
    );
  }

  getAllInvoices(): Observable<InvoiceSummary[]> {
    return this.http.get<InvoiceSummary[]>(this.base);
  }

  getInvoiceById(id: number): Observable<InvoiceDetail> {
    return this.http.get<InvoiceDetail>(`${this.base}/${id}`);
  }

  resubmitInvoice(id: number, payload: FbrInvoicePayload): Observable<any> {
    return this.http.put(`${this.base}/${id}/resubmit`, payload);
  }
}
