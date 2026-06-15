import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BuyerBusiness, BuyerProfile, CreateBuyerRequest } from '../models/buyer.model';

@Injectable({ providedIn: 'root' })
export class BuyerService {

  private base = 'http://localhost:8081/api/buyers';

  constructor(private http: HttpClient) {}

  listAll(): Observable<BuyerProfile[]> {
    return this.http.get<BuyerProfile[]>(this.base);
  }

  getByNtnCnic(ntnCnic: string): Observable<BuyerProfile> {
    return this.http.get<BuyerProfile>(`${this.base}/${ntnCnic}`);
  }

  create(req: CreateBuyerRequest): Observable<BuyerProfile> {
    return this.http.post<BuyerProfile>(this.base, req);
  }

  addBusiness(ntnCnic: string, b: Partial<BuyerBusiness>): Observable<BuyerProfile> {
    return this.http.post<BuyerProfile>(`${this.base}/${ntnCnic}/businesses`, b);
  }

  updateBusiness(ntnCnic: string, id: number, b: Partial<BuyerBusiness>): Observable<BuyerProfile> {
    return this.http.put<BuyerProfile>(`${this.base}/${ntnCnic}/businesses/${id}`, b);
  }

  deleteBusiness(ntnCnic: string, id: number): Observable<BuyerProfile> {
    return this.http.delete<BuyerProfile>(`${this.base}/${ntnCnic}/businesses/${id}`);
  }

  refreshRegType(ntnCnic: string): Observable<BuyerProfile> {
    return this.http.post<BuyerProfile>(`${this.base}/${ntnCnic}/refresh-reg-type`, {});
  }
}
