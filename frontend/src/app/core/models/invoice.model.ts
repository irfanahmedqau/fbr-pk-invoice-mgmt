export interface FbrItem {
  hsCode: string;
  productDescription: string;
  rate: string;
  uoM: string;
  quantity: number;
  totalValues: number;
  valueSalesExcludingST: number;
  fixedNotifiedValueOrRetailPrice: number;
  salesTaxApplicable: number;
  salesTaxWithheldAtSource: number;
  extraTax: string;
  furtherTax: number;
  sroScheduleNo: string;
  fedPayable: number;
  discount: number;
  saleType: string;
  sroItemSerialNo: string;
}

export interface FbrInvoicePayload {
  invoiceType: string;
  invoiceDate: string;
  sellerNTNCNIC: string;
  sellerBusinessName: string;
  sellerProvince: string;
  sellerAddress: string;
  buyerNTNCNIC: string;
  buyerBusinessName: string;
  buyerProvince: string;
  buyerAddress: string;
  buyerRegistrationType: string;
  invoiceRefNo: string;
  scenarioId: string;
  items: FbrItem[];
}

export interface ExcelRowResult {
  rowNumber: number;
  invoiceRefNo: string;
  status: 'SUCCESS' | 'FAILED';
  message: string;
  fbrResponse: any;
}

export interface BulkUploadResult {
  totalRows: number;
  successCount: number;
  failedCount: number;
  results: ExcelRowResult[];
}

export interface HsCode {
  hS_CODE: string;
  description: string;
}

export type InvoiceStatus = 'PENDING' | 'VALIDATED' | 'POSTED' | 'FAILED';

export interface InvoiceSummary {
  id: number;
  invoiceRefNo: string;
  invoiceType: string;
  invoiceDate: string;
  sellerNTNCNIC: string;
  sellerBusinessName: string;
  buyerNTNCNIC: string;
  buyerBusinessName: string;
  buyerProvince: string;
  scenarioId: string;
  status: InvoiceStatus;
  processedAt: string;
  validationResponse: string;
  fbrInvRefNumber?: string;
}

export interface InvoiceDetail extends InvoiceSummary {
  sellerProvince: string;
  sellerAddress: string;
  buyerAddress: string;
  buyerRegistrationType: string;
  invoiceRefNo: string;
  items: FbrItem[];
}

export interface BuyerAtlResult {
  active: boolean;
  [key: string]: any;
}

export interface BuyerRegTypeResult {
  registrationType: string;
  [key: string]: any;
}
