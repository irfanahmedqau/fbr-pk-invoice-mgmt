export interface BuyerBusiness {
  id: number;
  businessName: string;
  address: string;
  province: string;
  isDefault: boolean;
}

export interface BuyerProfile {
  id: number;
  ntnCnic: string;
  regType: string | null;
  regTypeLastChecked: string | null;
  businesses: BuyerBusiness[];
}

export interface CreateBuyerRequest {
  ntnCnic: string;
  businessName: string;
  address: string;
  province: string;
}
