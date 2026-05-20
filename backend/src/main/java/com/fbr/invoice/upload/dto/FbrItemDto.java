package com.fbr.invoice.upload.dto;

import java.util.Objects;




public class FbrItemDto {
    private String hsCode;
    private String productDescription;
    private String rate;
    private String uom;
    private double quantity;
    private double totalValue;
    private double valueSalesExcludingSt;
    private double fixedNotifiedValueOrRetailPrice;
    private double salesTaxApplicable;
    private double salesTaxWithheldAtSource;
    private String extraTax;
    private double furtherTax;
    private String sroScheduleNo;
    private double fedPayable;
    private double discount;
    private String saleType;
    private String sroItemSerialNo;
	public String getHsCode() {
		return hsCode;
	}
	public void setHsCode(String hsCode) {
		this.hsCode = hsCode;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public String getUom() {
		return uom;
	}
	public void setUom(String uom) {
		this.uom = uom;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	public double getTotalValue() {
		return totalValue;
	}
	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}
	public double getValueSalesExcludingSt() {
		return valueSalesExcludingSt;
	}
	public void setValueSalesExcludingSt(double valueSalesExcludingSt) {
		this.valueSalesExcludingSt = valueSalesExcludingSt;
	}
	public double getFixedNotifiedValueOrRetailPrice() {
		return fixedNotifiedValueOrRetailPrice;
	}
	public void setFixedNotifiedValueOrRetailPrice(double fixedNotifiedValueOrRetailPrice) {
		this.fixedNotifiedValueOrRetailPrice = fixedNotifiedValueOrRetailPrice;
	}
	public double getSalesTaxApplicable() {
		return salesTaxApplicable;
	}
	public void setSalesTaxApplicable(double salesTaxApplicable) {
		this.salesTaxApplicable = salesTaxApplicable;
	}
	public double getSalesTaxWithheldAtSource() {
		return salesTaxWithheldAtSource;
	}
	public void setSalesTaxWithheldAtSource(double salesTaxWithheldAtSource) {
		this.salesTaxWithheldAtSource = salesTaxWithheldAtSource;
	}
	public String getExtraTax() {
		return extraTax;
	}
	public void setExtraTax(String extraTax) {
		this.extraTax = extraTax;
	}
	public double getFurtherTax() {
		return furtherTax;
	}
	public void setFurtherTax(double furtherTax) {
		this.furtherTax = furtherTax;
	}
	public String getSroScheduleNo() {
		return sroScheduleNo;
	}
	public void setSroScheduleNo(String sroScheduleNo) {
		this.sroScheduleNo = sroScheduleNo;
	}
	public double getFedPayable() {
		return fedPayable;
	}
	public void setFedPayable(double fedPayable) {
		this.fedPayable = fedPayable;
	}
	public double getDiscount() {
		return discount;
	}
	public void setDiscount(double discount) {
		this.discount = discount;
	}
	public String getSaleType() {
		return saleType;
	}
	public void setSaleType(String saleType) {
		this.saleType = saleType;
	}
	public String getSroItemSerialNo() {
		return sroItemSerialNo;
	}
	public void setSroItemSerialNo(String sroItemSerialNo) {
		this.sroItemSerialNo = sroItemSerialNo;
	}
	@Override
	public int hashCode() {
		return Objects.hash(discount, extraTax, fedPayable, fixedNotifiedValueOrRetailPrice, furtherTax, hsCode,
				productDescription, quantity, rate, saleType, salesTaxApplicable, salesTaxWithheldAtSource,
				sroItemSerialNo, sroScheduleNo, totalValue, uom, valueSalesExcludingSt);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FbrItemDto other = (FbrItemDto) obj;
		return Double.doubleToLongBits(discount) == Double.doubleToLongBits(other.discount)
				&& Objects.equals(extraTax, other.extraTax)
				&& Double.doubleToLongBits(fedPayable) == Double.doubleToLongBits(other.fedPayable)
				&& Double.doubleToLongBits(fixedNotifiedValueOrRetailPrice) == Double
						.doubleToLongBits(other.fixedNotifiedValueOrRetailPrice)
				&& Double.doubleToLongBits(furtherTax) == Double.doubleToLongBits(other.furtherTax)
				&& Objects.equals(hsCode, other.hsCode) && Objects.equals(productDescription, other.productDescription)
				&& Double.doubleToLongBits(quantity) == Double.doubleToLongBits(other.quantity)
				&& Objects.equals(rate, other.rate) && Objects.equals(saleType, other.saleType)
				&& Double.doubleToLongBits(salesTaxApplicable) == Double.doubleToLongBits(other.salesTaxApplicable)
				&& Double.doubleToLongBits(salesTaxWithheldAtSource) == Double
						.doubleToLongBits(other.salesTaxWithheldAtSource)
				&& Objects.equals(sroItemSerialNo, other.sroItemSerialNo)
				&& Objects.equals(sroScheduleNo, other.sroScheduleNo)
				&& Double.doubleToLongBits(totalValue) == Double.doubleToLongBits(other.totalValue)
				&& Objects.equals(uom, other.uom) && Double.doubleToLongBits(valueSalesExcludingSt) == Double
						.doubleToLongBits(other.valueSalesExcludingSt);
	}
    
    
    
}
