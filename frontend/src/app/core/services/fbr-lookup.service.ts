import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import { HsCode } from '../models/invoice.model';

@Injectable({ providedIn: 'root' })
export class FbrLookupService {

  private base = 'http://localhost:8081/api/fbr';

  // Cached observables — fetched once per session
  private hsCodes$: Observable<HsCode[]> | null = null;
  private provinces$: Observable<any[]>   | null = null;
  private uom$: Observable<any[]>         | null = null;

  constructor(private http: HttpClient) {}

  getProvinces(): Observable<any[]> {
    if (!this.provinces$) {
      this.provinces$ = this.http.get<any[]>(`${this.base}/provinces`).pipe(shareReplay(1));
    }
    return this.provinces$;
  }

  getUom(): Observable<any[]> {
    if (!this.uom$) {
      this.uom$ = this.http.get<any[]>(`${this.base}/uom`).pipe(shareReplay(1));
    }
    return this.uom$;
  }

  getTransactionTypes(): Observable<any> {
    return this.http.get(`${this.base}/transaction-types`);
  }

  /** Cached — large list, load once per session. */
  getHsCodes(): Observable<HsCode[]> {
    if (!this.hsCodes$) {
      this.hsCodes$ = this.http.get<HsCode[]>(`${this.base}/hs-codes`).pipe(shareReplay(1));
    }
    return this.hsCodes$;
  }

  getBuyerRegType(ntn: string): Observable<any> {
    return this.http.get(`${this.base}/buyer-reg-type`, { params: { ntn } });
  }

  getBuyerAtl(ntn: string, date: string): Observable<any> {
    return this.http.get(`${this.base}/buyer-atl`, { params: { ntn, date } });
  }

  getExcelSheets(file: File): Observable<string[]> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<string[]>(`${this.base}/excel-sheets`, formData);
  }
}
